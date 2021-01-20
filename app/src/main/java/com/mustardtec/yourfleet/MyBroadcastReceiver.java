//package com.mustardtec.yourfleet;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.RemoteException;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.android.installreferrer.api.InstallReferrerClient;
//import com.android.installreferrer.api.InstallReferrerStateListener;
//import com.android.installreferrer.api.ReferrerDetails;
//
//public class MyBroadcastReceiver extends BroadcastReceiver {
//    private static final String TAG = "MyBroadcastReceiver";
//    InstallReferrerClient referrerClient;
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        Log.d(TAG, "here");
//        referrerClient = InstallReferrerClient.newBuilder(context).build();
//        referrerClient.startConnection(new InstallReferrerStateListener() {
//            @Override
//            public void onInstallReferrerSetupFinished(int responseCode) {
//                Log.d(TAG, "here1");
//                switch (responseCode) {
//                    case InstallReferrerClient.InstallReferrerResponse.OK:
//                        try {
//                            Log.d(TAG, "here2");
//                            ReferrerDetails response = referrerClient.getInstallReferrer();
//                            String ref = response.getInstallReferrer();
//                            Log.d(TAG, response.toString());
//                            referrerClient.endConnection();
//                        }
//                        catch (RemoteException ex) {
//                            Log.d(TAG, "ERROR:" + ex.getMessage());
//                        }
//
//                        // Connection established
//                        break;
//                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
//                        Log.d(TAG, "ERROR:FEATURE_NOT_SUPPORTED");
//                        // API not available on the current Play Store app
//                        break;
//                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
//                        Log.d(TAG, "ERROR:SERVICE_UNAVAILABLE");
//                        break;
//                }
//
//            }
//
//            @Override
//            public void onInstallReferrerServiceDisconnected() {
//                // Try to restart the connection on the next request to
//                // Google Play by calling the startConnection() method.
//            }
//        });
////        StringBuilder sb = new StringBuilder();
////        sb.append("Action: " + intent.getAction() + "\n");
////        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
////        String log = sb.toString();
////        Log.d(TAG, log);
////        Toast.makeText(context, log, Toast.LENGTH_LONG).show();
//    }
//}