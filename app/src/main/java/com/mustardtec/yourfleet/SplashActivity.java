package com.mustardtec.yourfleet;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.RemoteException;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.util.List;

import bolts.AppLinks;


/**
 * Created by andy on 31/03/2017.
 */

public class SplashActivity extends AppCompatActivity implements InstallReferrerStateListener {
    private static final String TAG = "RSD";
    InstallReferrerClient mReferrerClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        mReferrerClient = InstallReferrerClient.newBuilder(this).build();
        mReferrerClient.startConnection(this);

        super.onCreate(savedInstanceState);

        try {
            Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
            String referrer = "";
            if (targetUrl != null) {
                Bundle applinkData = AppLinks.getAppLinkData(getIntent());
                referrer = applinkData.getString("referral");
                if (referrer == null) {
                    Uri data = getIntent().getData();
                    referrer = data.getQueryParameter("referral");
                }
                if (referrer != "") {
                    ((appGlobal) getApplication()).setAppReferrer(referrer);
                }

            }
        }
        catch (Exception ex) {

        }


        try {
            Uri data = getIntent().getData();
            String scheme = data.getScheme(); // "http"
            if (scheme == "know.your.car") {
                String host = data.getHost(); // "twitter.com"

                List<String> params = data.getPathSegments();
                String first = params.get(0); // "status"
                String second = params.get(1); // "1234"
            }
        }
        catch (Exception ex)
        {

        }




       // startActivity(intent);
      //  finish();
    }
    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        String referrerData = "";
        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                try {
                    ReferrerDetails response = mReferrerClient.getInstallReferrer();
                    referrerData = response.getInstallReferrer();
                    mReferrerClient.endConnection();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                Log.w(TAG, "InstallReferrer not supported");
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                Log.w(TAG, "Unable to connect to the service");
                break;
            default:
                Log.w(TAG, "responseCode not found.");
        }
        RunUpMainApp(this, referrerData);

    }
    @Override
    public void onInstallReferrerServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
//        stringBuilder.append("\nonInstallReferrerServiceDisconnected. attemptCount = " + attemps);
//        stringBuilder.append("\nisReady == " + mReferrerClient.isReady());
//
//        if (attemps < 3) {
//            attemps++;
//            stringBuilder.append("\nonInstallReferrerServiceDisconnected. RE-startConnection");
//            mReferrerClient.startConnection(this);
//        } else {
//            stringBuilder.append("\nonInstallReferrerServiceDisconnected. endConnection");
//            stringBuilder.append("\nisReady == " + mReferrerClient.isReady());
//            mReferrerClient.endConnection();
//            stringBuilder.append("\nendConnection");
//            stringBuilder.append("\nisReady == " + mReferrerClient.isReady());
//        }
//
//        txtBody.setText(stringBuilder.toString());
        try {
            mReferrerClient.endConnection();
        }
        catch (Exception ex) {
            Log.w(TAG, "onInstallReferrerServiceDisconnected errored.");
        }
    }

    private void RunUpMainApp(SplashActivity splashActivity, String referrerData) {
        String notifyData = "";

        try {
            if (splashActivity.getIntent().getExtras() != null) {
                for (String key : splashActivity.getIntent().getExtras().keySet()) {
                    String value = splashActivity.getIntent().getExtras().getString(key);
                    if (key.equals("NotifyData")) {
                        notifyData = value;
                    }
                    // Log.d("SplashActivity", "BundleKey: " + key + " Value: " + value);
                }
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "RunUpMainApp: ex:" + ex.getMessage());
        }

        Intent intent = new Intent(splashActivity, MainActivity.class);
        if (!notifyData.equals("")) {
            intent.putExtra("NotifyData", notifyData);
        }
        if (!referrerData.equals("")) {
            intent.putExtra("ReferrerData", referrerData);
        }
        splashActivity.startActivity(intent);
        splashActivity.finish();

    }


}
