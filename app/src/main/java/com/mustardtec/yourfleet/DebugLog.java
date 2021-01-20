package com.mustardtec.yourfleet;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;
/**
 * Created by dan on 22/09/2017.
 */

public class DebugLog {
    //Context context;

    private Context mCon;
    private String AndroidId = "";
    private String UserId = "";
    private boolean SendDebugs = false;

    public HtmlAPI mHtmlAPI;


    public DebugLog(Context pCon){
        mCon = pCon;
        AndroidId = Settings.Secure.getString(mCon.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (((appGlobal) mCon.getApplicationContext()).getShouldDebug())
        {
            SendDebugs = true;
        }
        try {
            UserId = ((appGlobal) mCon.getApplicationContext()).getAppUserID();
        }
        catch (Exception ex)
        {

        }
    }
    public void d (String pTag, String pMessage){
        Log.d(pTag,pMessage);
        String lMess = pMessage;
        try {
            lMess = lMess.replaceAll("[^A-Za-z0-9]", "");
//            lMess = URLEncoder.encode(pMessage, "UTF-8");
//            lMess = lMess.replace("{", "").replace("}","").replace("'","").replace("+","|");
        }
        catch (Exception ex) {}

        if (mHtmlAPI != null && (((appGlobal) mCon.getApplicationContext()).getFrontEndDone())) {
            mHtmlAPI.WriteLog(pTag,lMess);
        } else {
         //   SendLog(pTag + pMessage);
        }
    }
    public void e (String pTag, String pMessage){
        Log.e(pTag,pMessage);
        String lMess = pMessage;
        try {
            lMess = lMess.replaceAll("[^A-Za-z0-9]", "");
            //lMess = URLEncoder.encode(pMessage, "UTF-8");
            //lMess = lMess.replace("{", "").replace("}","").replace("'","").replace("+","|");
        }
        catch (Exception ex) {

        }
        if (mHtmlAPI != null && (((appGlobal) mCon.getApplicationContext()).getFrontEndDone())) {
            mHtmlAPI.WriteLog(pTag,lMess);
        } else {
            SendLog(pTag + lMess);
        }
//        if (((appGlobal) mCon.getApplicationContext()).getShouldDebug())
//        {
//            SendDebugs = true;
//        }
//        if (SendDebugs)
//            SendLog(pTag + pMessage);
    }
    public void DebugSendLog(String pTag, final String pMessage) {
        try {
            Log.d(pTag, pMessage);
            String lMess = pMessage;
            try {
                lMess = lMess.replaceAll("[^A-Za-z0-9]", "");
                //lMess = URLEncoder.encode(pMessage, "UTF-8");
               //lMess = lMess.replace("{", "").replace("}","").replace("'","").replace("+","|");
            }
            catch (Exception ex) {

            }
            WebAPI webApi = new WebAPI(this.mCon);
            webApi.Controller = "app";
            webApi.Action = "updatelog";
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            String[] sparams = {lMess, currentDateTimeString};
            webApi.RunIt(sparams, new WebAPI.onWebAPIReturnListener() {
                public void onFinished(String result) {

                }
            });
        }
        catch (Exception ex)
        {

        }

    }
    public void SendLog(final String pMessage) {
        try {
            String lMess = pMessage;
            try {
                lMess = lMess.replaceAll("[^A-Za-z0-9]", "");
                //lMess = URLEncoder.encode(pMessage, "UTF-8");
               // lMess = lMess.replace("{", "").replace("}","").replace("'","");
            }
            catch (Exception ex) {

            }

            WebAPI webApi = new WebAPI(this.mCon);
            webApi.Controller = "app";
            webApi.Action = "updatelog";
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            String[] sparams = {lMess, currentDateTimeString};
            webApi.RunIt(sparams, new WebAPI.onWebAPIReturnListener() {
                public void onFinished(String result) {

                }
            });
        }
        catch (Exception ex) {

        }

    }
}