package com.example.ayazshah.faceassistantglassapp;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.security.ProviderInstaller;

/**
 * Created by AyazShah on 5/8/17.
 */

public class FaceApplication extends Application {

    public static final String TAG = FaceApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        GoogleAPIHelper.init(this);
    }
}
