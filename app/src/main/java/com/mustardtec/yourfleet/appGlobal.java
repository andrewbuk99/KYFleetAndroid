package com.mustardtec.yourfleet;


import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

/**
 * Created by andy on 04/01/2016.
 */
public class appGlobal extends MultiDexApplication {

    private String appReady = "";
    private String appUserID = "";
    private String appVersion;
    private String appSubVersion;
    private String html5Version = "";
    private String appDeviceId = "";
    private String appAuthToken = "";
    private String appMode = "";
    private String appType = "";
    private String appHtmlStr = "";
    private String appGovStr = "";
    private String appReferrer = "";
    private boolean ShouldDebug = false;
    private boolean HtmlRunningStatus = false;
    private boolean FrontEndDone = false;
    private boolean FrontEndCanRun = false;

    public String getHtmlStr() {
        return appHtmlStr;
    }

    public void setHtmlStr(String appHtmlStr) {
        this.appHtmlStr = appHtmlStr;
    }

    public String getGovStr() {
        return appGovStr;
    }

    public void setGovStr(String appGovStr) {
        this.appGovStr = appGovStr;
    }

    public boolean getHtmlRunningStatus() {
        return HtmlRunningStatus;
    }

    public void setHtmlRunningStatus(boolean appHtmlStr) {
        this.HtmlRunningStatus = appHtmlStr;
    }

    public boolean getFrontEndDone() {
        return FrontEndDone;
    }

    public void setFrontEndDone(boolean FrontEndDone) {
        this.FrontEndDone = FrontEndDone;
    }

    public boolean getFrontEndCanRun() {
        return FrontEndCanRun;
    }

    public void setFrontEndCanRun(boolean FrontEndCanRun) {
        this.FrontEndCanRun = FrontEndCanRun;
    }
    public boolean getShouldDebug() {
        return ShouldDebug;
    }

    public void setShouldDebug(boolean ShouldDebug) {
        this.ShouldDebug = ShouldDebug;
    }

    public String getFrontEndReady() {
        return appReady;
    }

    public void setFrontEndReady(String appReady) {
        this.appReady = appReady;
    }

    public String getHTML5Version() {
        return html5Version;
    }

    public void setHTMl5Version(String appVersion) {
        this.html5Version = appVersion;
    }
    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppSubVersion() {
        return appSubVersion;
    }

    public void setAppSubVersion(String appSubVersion) {
        this.appSubVersion = appSubVersion;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppReferrer() {
        return appReferrer;
    }

    public void setAppReferrer(String appReferrer) {
        this.appReferrer = appReferrer;
    }

    public String getAppMode() {
        return appMode;
    }

    public void setAppMode(String appMode) {
        this.appMode = appMode;
    }
    public String getAppUserID() {
        return appUserID;
    }

    public void setAppUserID(String sUserID) {
        Boolean UserChanged = false;
        if (sUserID != appUserID) {
            UserChanged = true;
        }
        this.appUserID = sUserID;
        if (UserChanged) {
            // send notification token to new user account
            final Context mCont = this.getApplicationContext();
            final WebAPI webApi = new WebAPI(mCont);
            final String UserId = ((appGlobal) mCont.getApplicationContext()).getAppUserID();
            final String PlatformID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                              //  Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            if ((UserId != "") && (Integer.parseInt(UserId) != 0) && (token != null) && (!token.isEmpty())) {
                                webApi.Controller = "app";
                                webApi.Action = "UpdateNotificationToken";
                                String[] sparams = {token, PlatformID, "", "", "", ""};
                                webApi.RunIt(sparams, new WebAPI.onWebAPIReturnListener() {
                                    public void onFinished(String result) {

                                    }
                                });
                            }

                        }
                    });
          //  String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        }
    }

    public String getAppDeviceId() {
        return appDeviceId;
    }

    public void setAppDeviceId(String appDeviceId) {
        this.appDeviceId = appDeviceId;
    }

    public String getAuthToken() {
        return appAuthToken;
    }

    public void setAuthToken(String appAuthToken) {
        this.appAuthToken = appAuthToken;
    }
}
