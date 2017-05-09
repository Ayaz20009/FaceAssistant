package com.example.ayazshah.faceassistantglassapp;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by AyazShah on 5/8/17.
 */

public interface TokenRequestListener {

    void onTokenReceived(GoogleSignInAccount account);
    void onFailedToGetToken();

}
