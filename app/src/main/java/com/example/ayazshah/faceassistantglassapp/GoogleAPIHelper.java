package com.example.ayazshah.faceassistantglassapp;

/**
 * Created by AyazShah on 5/8/17.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;


/**
 * Created by QiFeng on 3/21/17.
 */

public class GoogleAPIHelper {

    public static final String TAG = GoogleAPIHelper.class.getSimpleName();

    private final GoogleApiClient mSilentSignInClient;

    private static GoogleAPIHelper mGoogleAPIHelper;

    //TEST :
    // 2 accounts verified
    // what if not logged in. does this get token correctly ?

    public static GoogleAPIHelper init(Context context) {
        if (mGoogleAPIHelper == null) mGoogleAPIHelper = new GoogleAPIHelper(context);
        return mGoogleAPIHelper;
    }

    public static GoogleAPIHelper getInstance() {
        return mGoogleAPIHelper;
    }

    private GoogleAPIHelper(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.client_id))
                .requestEmail()
                .build();

        mSilentSignInClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }


    public void makeApiRequest(final TokenRequestListener tokenRequestListener) {
        OptionalPendingResult<GoogleSignInResult> optionalPendingResult =
                Auth.GoogleSignInApi.silentSignIn(mSilentSignInClient);

        if (optionalPendingResult.isDone()) {//have token
            Log.i(TAG, "makeApiRequest: ");
            GoogleSignInResult result = optionalPendingResult.get();
            verifyResult(result, tokenRequestListener);
        } else {
            Log.d(TAG, "makeApiRequest: expired token");
            optionalPendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    verifyResult(googleSignInResult, tokenRequestListener);
                    mSilentSignInClient.disconnect();
                }
            });

            mSilentSignInClient.connect();
        }
    }

    private void verifyResult(GoogleSignInResult result, TokenRequestListener listener) {
        if (result.isSuccess()) {
            listener.onTokenReceived(result.getSignInAccount());
        } else {
            listener.onFailedToGetToken();
        }
    }


}