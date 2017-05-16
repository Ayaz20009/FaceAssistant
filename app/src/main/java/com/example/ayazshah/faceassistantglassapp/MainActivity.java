package com.example.ayazshah.faceassistantglassapp;

import com.google.android.glass.content.Intents;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.gms.vision.text.Text;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;

import static android.R.attr.data;
import static android.R.attr.theme;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TAKE_PICTURE_REQUEST = 1;
    private GestureDetector mGestureDetector = null;
    private CameraView cameraView;
    private String encoded_string, image_path;
    private Bitmap bitmap;
    private ProgressBar spinner;
    private TextView mName;
    private TextView mConfidence;
    private ImageView mImageView;
    private TextView mRelation;
    private TextView mNotes;
    private TextView mLastVisited;
    private Profile mProfile;
    private LovedOnes mLovedones;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = createGestureDetector(this);
        //this.setContentView(cameraView);
        setContentView(R.layout.activity_main);
        cameraView = (CameraView) findViewById(R.id.camerapp);
        cameraView.setVisibility(View.VISIBLE);
        spinner = (ProgressBar)findViewById(R.id.progress);
        spinner.setVisibility(View.INVISIBLE);
        mName = (TextView) findViewById(R.id.name);
        mConfidence = (TextView) findViewById(R.id.confidence);
        mRelation = (TextView) findViewById(R.id.relation);
        mNotes = (TextView) findViewById(R.id.notes);
        mLastVisited = (TextView) findViewById(R.id.lastVisited);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraView != null) {
            cameraView.releaseCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (cameraView != null) {
            cameraView.releaseCamera();
        }
    }


    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (cameraView != null) {
                    // Tap with a single finger for photo
                    if (gesture == Gesture.TAP) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
                        return true;
                    }
                }
                return false;
            }
        });

        return gestureDetector;
    }
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            image_path = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
            processPictureWhenReady(image_path);
            cameraView.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class Encode_image extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                bitmap = Utils.decodeUri(MainActivity.this, Uri.fromFile(new File(image_path)), 200);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                Log.i(TAG, "doInBackground: " + bitmap.getWidth() + " " + bitmap.getHeight());

                byte[] array = stream.toByteArray();
                encoded_string = Base64.encodeToString(array, 0);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "onPostExecute: ");
            makeRequest("106378594523530379739");
        }
    }
    private void makeRequest(String token) {

        if (encoded_string == null) {
            Log.e(TAG, "makeRequest: encoded string was null");
            return;
        }

        HashMap<String, String> header = API.getMainHeader(token);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("image", encoded_string);


        API.post(new String[]{"face", "infer"}, header, params,
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "onFailure: ");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                        try {
                            String jsonData = response.body().string();
                            Log.i("Response",jsonData);
                            if (response.isSuccessful()) {
                                String header = response.header("Person-Type", null);
                                if (header.equals("celeb")){
                                    mProfile = getProfileDetails(jsonData);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            spinner.setVisibility(View.GONE);
                                            updateProfiles();

                                        }
                                    });
                                }
                                if (header.equals("loved-one")){
                                    Log.i("Loved One","Loved");
                                    mLovedones = getLovedOnesDetails(jsonData);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateLovedOnes();
                                        }
                                    });
                                }


                                //Log.i("Response", response.body().string());
                            }
                        }catch (IOException e){
                            Log.e (TAG,"Exception caught",e);
                        }catch (JSONException e){
                            Log.e (TAG,"Exception caught",e);
                        }
                   }

                });

    }



    private LovedOnes getLovedOnesDetails(String jsonData) throws JSONException {
        JSONObject loved_one = new JSONObject(jsonData);
        String name = loved_one.getString("name");
        Log.i(TAG,"From JSON:" + name);
        String birthday = loved_one.getString("birthday");
        Log.i(TAG,"From JSON:" + birthday);
        String relationship = loved_one.getString("relationship");
        Log.i(TAG,"From JSON:" + relationship);
        String note = loved_one.getString("note");
        Log.i(TAG,"From JSON:" + note);
        String last_viewed = loved_one.getString("last_viewed");
        Log.i(TAG,"From JSON:" + last_viewed);
        LovedOnes lovedOnes = new LovedOnes();
        lovedOnes.setName(name);
        lovedOnes.setBirthday(birthday);
        lovedOnes.setNotes(note);
        lovedOnes.setRelation(relationship);
        lovedOnes.setLastSeen(last_viewed);
        return lovedOnes;
    }


    private void updateLovedOnes() {

        Log.i("Name", mLovedones.getName());
        Log.i("Name", mLovedones.getBirthday());
        Log.i("Name", mLovedones.getLastSeen());
        Log.i("Name", mLovedones.getNotes());
        Log.i("Name", mLovedones.getRelation());

    }

    private Profile getProfileDetails(String jsonData) throws JSONException {
            JSONObject profile = new JSONObject(jsonData);
            String name = profile.getString("name");
            Log.i(TAG,"From JSON:" + name);
            String confidence = profile.getString("confidence");
            Log.i(TAG,"From JSON confidence :" + confidence);
            Profile nprofile = new Profile();
            nprofile.setName(name);
            nprofile.setConfidence(confidence);
            return nprofile;
    }
    private void updateProfiles() {
        mName.setText(mProfile.getName());
        mConfidence.setText(mProfile.getConfidence());
        Log.i("Name", mProfile.getName());
        Log.i("Confidence",mProfile.getConfidence());

    }


    private void processPictureWhenReady(final String picturePath) {
    final File pictureFile = new File(picturePath);

    if (pictureFile.exists()) {
        Log.i(TAG, "processPictureWhenReady: filexists");
//            try {
//                nbitmap = Utils.decodeUri(MainActivity.this, Uri.fromFile(new File(image_path)), 200);
//                mFaceOverlayView.setBitmap(nbitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        new Encode_image().execute();
    } else {
        // The file does not exist yet. Before starting the file observer, you
        // can update your UI to let the user know that the application is
        // waiting for the picture (for example, by displaying the thumbnail
        // image and a progress indicator).
        Log.i(TAG, "Doesn't exist: No file");
        final File parentDirectory = pictureFile.getParentFile();
        FileObserver observer = new FileObserver(parentDirectory.getPath(),
                FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
            // Protect against additional pending events after CLOSE_WRITE
            // or MOVED_TO is handled.
            private boolean isFileWritten;

            @Override
            public void onEvent(int event, String path) {
                if (!isFileWritten) {
                    // For safety, make sure that the file that was created in
                    // the directory is actually the one that we're expecting.
                    File affectedFile = new File(parentDirectory, path);
                    isFileWritten = affectedFile.equals(pictureFile);

                    if (isFileWritten) {
                        stopWatching();

                        // Now that the file is ready, recursively call
                        // processPictureWhenReady again (on the UI thread).
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processPictureWhenReady(picturePath);
                            }
                        });
                    }
                }
            }
        };
        observer.startWatching();
    }
}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {

            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}