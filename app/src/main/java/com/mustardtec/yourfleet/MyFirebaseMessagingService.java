/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mustardtec.yourfleet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.provider.Settings;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String ACTION_COMPLETE=
            "com.mustardtec.yourfleet.action.COMPLETE";
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        Context context = getApplicationContext();
        DebugLog debugLog = new DebugLog(context);
        debugLog.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {

            Intent resultIntent = new Intent(ACTION_COMPLETE);
            resultIntent.putExtra("title", remoteMessage.getNotification().getTitle());
            resultIntent.putExtra("body", remoteMessage.getNotification().getBody());
            if (remoteMessage.getData().size() > 0) {
                debugLog.d(TAG, "Message data payload: " + remoteMessage.getData());
                try {
                    JSONObject jRes = new JSONObject(remoteMessage.getData().toString());
                    String notifyData = jRes.get("NotifyData").toString();
                    if (!notifyData.equals("")) {
                        resultIntent.putExtra("NotifyData", notifyData);
                    }
                } catch (Exception ex) {
                    debugLog.e(TAG, " remove Message data payload error: " + ex.getMessage());
                }
            }
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(resultIntent);
        } else {

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                debugLog.d(TAG, "Message data payload: " + remoteMessage.getData());

//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
//            } else {
                // Handle message within 10 seconds
                try {
                    Intent resultIntent = new Intent(ACTION_COMPLETE);
                    JSONObject jRes = new JSONObject(remoteMessage.getData().toString());
                    String notifyData = jRes.get("NotifyData").toString();
                    if (!notifyData.equals("")) {
                        resultIntent.putExtra("NotifyData", notifyData);
                    }

                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(resultIntent);
                } catch (Exception ex) {
                    debugLog.e(TAG, " remove Message data payload error: " + ex.getMessage());
                }
//            }

            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            debugLog.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(String data) {
        Log.d(TAG, "Short lived task is done.");

    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    private void sendRegistrationToServer(String token) {

        Context mCont = this.getApplicationContext();
        WebAPI webApi = new WebAPI(mCont);
        String UserId = ((appGlobal) mCont.getApplicationContext()).getAppUserID();
        String PlatformID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "sendRegistrationToServer: " + token + " userid:"+UserId);
        if ((UserId != "") && (Integer.parseInt(UserId) != 0))  {
            webApi.Controller = "app";
            webApi.Action = "UpdateNotificationToken";
            String[] sparams = {token, PlatformID, "", "", "", ""};
            webApi.RunIt(sparams, new WebAPI.onWebAPIReturnListener() {
                public void onFinished(String result) {

                }
            });
        }


    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
