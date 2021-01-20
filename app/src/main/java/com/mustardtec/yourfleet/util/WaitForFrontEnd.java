package com.mustardtec.yourfleet.util;

//import android.app.Activity;
//import android.content.Context;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.mustardtec.yourfleet.MainActivity;
//import com.mustardtec.yourfleet.WebAPI;
//import com.mustardtec.yourfleet.appGlobal;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.net.URLEncoder;

/**
 * Created by danie on 29/09/2017.
 */

//public class WaitForFrontEnd extends AsyncTask<String, Integer, Integer> {
//    //Context context;
//    private MainActivity mAct;
//    private Context mCon;
//    private onReturnListener mListerner;
//    public WaitForFrontEnd(Activity act, Context con) {
//        mAct = (MainActivity)act;
//        mCon = con;
//
//    }
//    public interface onReturnListener {
//        /**
//         * Called to notify that setup is complete.
//         *
//         * @param result The result of the setup process.
//         */
//        void onFinished(String result);
//    }
//
//    public void RunIt(final onReturnListener listener) {
//        mListerner = listener;
//        execute("");
//    }
//
//
//    protected Integer doInBackground(String... vname) {
//        int notries = 500000;
//        int retry = 0;
//        int actualgoes = 0;
//        String tempApp = "";
//        boolean vfound = false;
//        long startTime = System.currentTimeMillis();
//        try {
//            do {
//                actualgoes++;
//                tempApp = ((appGlobal) this.mAct.getApplication()).getFrontEndReady();
//
//                if (tempApp != "" && tempApp != null) {
//                    //Log.d("WaitForFrontEnd", "appReady got: " + actualgoes);
//                    actualgoes=999999;
//                    vfound=true;
//                }
//            } while (((System.currentTimeMillis()-startTime)<10000) && !vfound);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return actualgoes;
//    }
//
//    protected void onProgressUpdate(Integer... progress) {
//        //setProgressPercent(progress[0]);
//    }
//    @Override
//    protected void onPostExecute(Integer result) {
//        super.onPostExecute(result);
//        ((appGlobal) mAct.getApplication()).setFrontEndDone(true);
//        try {
//            mAct.htmlAPI.setAndroidSubVersionRtn();
//
//        }
//        catch (Exception ex)
//        {
//            mAct.debugLog.e("ERROR: ANDROID:"," WaitForFrontEnd onPostExecute setAndroidSubVersionRtn");
//        }
//        try {
//            String referrer = ((appGlobal) mAct.getApplication()).getAppReferrer();
//            String sEncoded = URLEncoder.encode(referrer, "UTF-8");
//            mAct.htmlAPI.sendReferrer(sEncoded);
//        }
//        catch (Exception ex)
//        {
//            mAct.debugLog.e("ERROR: ANDROID:"," WaitForFrontEnd onPostExecute getAppReferrer");
//        }
//        if (result == 999999) {
//            WebAPI webApi = new WebAPI(mCon);
//            webApi.NotifyServerofopen(new WebAPI.onWebAPINotifyReturnListener() {
//                public void onFinished(String result) {
//                    if (result.equals("maintenance"))
//                    {
//                        mAct.htmlAPI.LoadMaintenanceScreen();
//                    }
//                    else if (result != "FAILED") {
//
//
//                        String sShouldDebug = "no";
//                        String sUserID = "";
//                        String sReLoad = "no";
//                        String sTimings = "5,15";
//                        String sAuthToken = "";
//                        String sAppMode = "";
//                        String sAppType = "";
//                        String sJWT = "";
//                        try {
//                            JSONObject jRes = new JSONObject(result);
//                            sUserID = jRes.get("UserId").toString();
//                            sAuthToken = jRes.get("AuthToken").toString();
//                            sAppMode = jRes.get("AppMode").toString();
//                            sAppType = jRes.get("AppType").toString();
//                            sShouldDebug = jRes.get("DebugRequired").toString();
//                            if (sShouldDebug.equalsIgnoreCase("yes"))
//                                ((appGlobal) mAct.getApplication()).setShouldDebug(true);
//                            sReLoad = jRes.get("Reload").toString();
//                            sTimings = jRes.get("timings").toString();
//                            sJWT = jRes.get("JWT").toString();
//
//                            mAct.debugLog.d("INFO: ANDROID:", "NotifyServerofopen: sUserID: " + sUserID + " sAuthToken: " + sAuthToken );
//
//                        } catch (JSONException e) {
//
//                            e.printStackTrace();
//                        }
//                        ((appGlobal) mAct.getApplication()).setAppUserID(sUserID);
//                        ((appGlobal) mAct.getApplication()).setAuthToken(sAuthToken);
//                        ((appGlobal) mAct.getApplication()).setAppMode(sAppMode);
//                        ((appGlobal) mAct.getApplication()).setAppType(sAppType);
//                        mAct.htmlAPI.JWTrtn(sJWT);
//                        mAct.htmlAPI.SetUserID(sUserID);
//
//
///*                        if (sShouldBill.toLowerCase().contains("ads")) {
//                            String[] parts = sTimings.split(",");
//                            //parts[0];
//                            mAct.ShouldIBill = "no";
//                            mAct.showAds = true;
//                            mAct.adFirst = Integer.parseInt(parts[0]);
//                            mAct.adSub = Integer.parseInt(parts[1]);
//                        }*/
//                        if (sReLoad.toLowerCase().contains("yes")) {
//                            mAct.mWebView.clearCache(true);
//                            ((appGlobal) mAct.getApplication()).setFrontEndReady("");
//                            //mAct.clearCacheFolder(mCon.getCacheDir());
//                            mAct.htmlAPI.LoadMainScreen(true);
//                        }
//                        mListerner.onFinished("OK");
//                    } else {
//                        mAct.htmlAPI.showError("Failed to contact our servers");
//                        mListerner.onFinished("FAILED");
//               //         System.exit(0);
//                    }
//                }
//            });
//        } else {
//            mListerner.onFinished("FAILED");
//        }
//
//    }
//}