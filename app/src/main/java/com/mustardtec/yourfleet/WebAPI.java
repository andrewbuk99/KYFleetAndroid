package com.mustardtec.yourfleet;

/**
 * Created by dan on 21/09/2017.
 */
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class WebAPI extends AsyncTask<String, String, String> {



    private Context mCont;
    private onWebAPIReturnListener mListerner;
    public String Controller;
    public String Referrer = "";
    public String Action;
    public String AndroidId = "";
    public String UserId = "";
    public String HTML5Version = "";
    public String AuthToken = "";
    public String AppVersion = "";
    public Integer Timeout = 60000;
    public DebugLog debugLog = null;
    public WebAPI(Context cont) {
        mCont = cont;
        debugLog = new DebugLog(mCont);
        AndroidId = ((appGlobal) this.mCont.getApplicationContext()).getAppDeviceId();

        UserId = ((appGlobal) this.mCont.getApplicationContext()).getAppUserID();
        HTML5Version = ((appGlobal) this.mCont.getApplicationContext()).getHTML5Version();
        AppVersion = ((appGlobal) this.mCont.getApplicationContext()).getAppVersion();
        AuthToken = ((appGlobal) this.mCont.getApplicationContext()).getAuthToken();
        if (AuthToken == "")
            AuthToken = "none";
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mCont.getSystemService(mCont.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void RunIt(final String[] sParams, final onWebAPIReturnListener listener) {
        mListerner = listener;
        execute(sParams);
    }


    public String BuildLookUpKeyJSON(String[] vcall) {
        String deviceIdToken = AndroidId;
        PwStorage hashing = new PwStorage();
        deviceIdToken = hashing.main(deviceIdToken);
        String key1= "";
        String key2= "";
        String key3= "";
        String key4= "";
        String key5= "";
        String key6= "";
        if(vcall != null) {
            if (vcall.length >= 1)
                key1 = vcall[0];
            if (vcall.length >= 2)
                key2 = vcall[1];
            if (vcall.length >= 3)
                key3 = vcall[2];
            if (vcall.length >= 4)
                key4 = vcall[3];
            if (vcall.length >= 5)
                key5 = vcall[4];
            if (vcall.length >= 6)
                key6 = vcall[5];
        }

        JSONObject jsonParam;
        jsonParam = new JSONObject();
        try {
            jsonParam.put("Key1", key1);
            jsonParam.put("Key2", key2);
            jsonParam.put("Key3", key3);
            jsonParam.put("Key4", key4);
            jsonParam.put("Key5", key5);
            jsonParam.put("Key6", key6);
            jsonParam.put("SearchKey", "[]");
            jsonParam.put("UserID", UserId);
            jsonParam.put("DeviceID", AndroidId);
            jsonParam.put("HTML5Version", HTML5Version);
            jsonParam.put("AppVersion", AppVersion);
            jsonParam.put("Platform", "android");
            jsonParam.put("Token", deviceIdToken);
            jsonParam.put("AuthToken", AuthToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonParam.toString();

    }
    protected String doInBackground(String... vcall) {
        HttpURLConnection urlConnection = null;
        String str ="";
        String retstr = "";
        int notries = 1;
        //if (Action == "CheckV3") notries = 25;
        int retry = 0;
        if (BuildConfig.DEBUG) {
            str = mCont.getResources().getString(R.string.mainDomain_debug) + "api/" + Controller + "/" + Action +"/";
        } else {
            str = mCont.getResources().getString(R.string.mainDomain) + "api/" + Controller + "/" + Action +"/";
        }
        try {


            do {
                if (isNetworkAvailable()) { // loading offline

                    URL urlToRequest = new URL(str);
                    urlConnection = (HttpURLConnection)
                            urlToRequest.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.setConnectTimeout(Timeout);
                    urlConnection.setReadTimeout(Timeout);
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());

                    String params = BuildLookUpKeyJSON(vcall);
                    if (!Action.equalsIgnoreCase("updatelog")) {
                        debugLog.d("INFO: ANDROID:", "WebAPI Calling BackEnd: Controller: " + Controller + " Action:" + Action);
                        debugLog.d("INFO: ANDROID:", "WebAPI Calling BackEnd: params: " + params);
                    }

                    out.write(params);
                    out.close();
                    // handle issues
                    int statusCode = 0;
                    try {
                        statusCode = urlConnection.getResponseCode();
                        retstr = "FAILED";
                        if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            // handle unauthorized (if service requires user login)
                            retry++;
                        } else if (statusCode != HttpURLConnection.HTTP_OK) {
                            retry++;
                        } else {
                            // create JSON object from content
                            InputStream in = new BufferedInputStream(
                                    urlConnection.getInputStream());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder result = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                result.append(line);
                            }
                            retry = 1000;
                            retstr = result.toString();
                        }
                    }
                    catch (IOException e)
                    {
                        if (!Action.equalsIgnoreCase("updatelog")) {
                            debugLog.e("ERROR: ANDROID:", "WebAPI Exception: " + e.getMessage());
                        }
                        if (e.getMessage().indexOf("timeout") > -1)
                        {
                            if (!Action.equalsIgnoreCase("updatelog")) {
                                debugLog.e("ERROR: ANDROID:", "WebAPI TIMEOUT Calling BackEnd: Controller: " + Controller + " Action:" + Action);
                            }
                            retstr = "TIMEOUT";
                            retry++;
                        } else {
                            if (!Action.equalsIgnoreCase("updatelog")) {
                                debugLog.e("ERROR: ANDROID:", "WebAPI RETURNING FAILED");
                            }
                            retstr = "FAILED";
                            retry = notries;
                        }
                    }

                } else {
                    // no network so load...
                    retry++;
                }
            } while (retry < notries);

        } catch (MalformedURLException e) {
            retstr = "FAILED";
            e.printStackTrace();
        } catch (IOException e) {
            retstr = "FAILED";
            e.printStackTrace();
        } catch (Exception ex) {
            retstr = "FAILED";
            ex.printStackTrace();
        }
        return retstr;
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    public interface onWebAPIReturnListener {
        /**
         * Called to notify that setup is complete.
         *
         * @param result The result of the setup process.
         */
        void onFinished(String result);
    }
    public interface onWebAPINotifyReturnListener {
        /**
         * Called to notify that setup is complete.
         *
         * @param result The result of the setup process.
         */
        void onFinished(String result);
    }

    public interface onPaymentWebAPIReturn {
        /**
         * Called to notify that an inventory query operation completed.
         *
         * @param result The result of the operation.
         */
        // void onQueryInventoryFinished(IabResult result, Inventory inv);
        void onPaymentFinished(String result);
    }
    @Override
    protected void onPostExecute(String result) {
        mListerner.onFinished(result);
    }



    public void NotifyServerofopen(final onWebAPINotifyReturnListener listener) {
        String ref = "";
        try {
            ((appGlobal) this.mCont.getApplicationContext()).getAppReferrer();
        }
        catch (Exception ex) {}
        String appv = "";
        try {
            appv = ((appGlobal) this.mCont.getApplicationContext()).getAppSubVersion();
        }
        catch (Exception ex) {}
        String[] sparams = {ref,"","","",appv,Settings.Secure.getString(this.mCont.getContentResolver(), Settings.Secure.ANDROID_ID)};
        this.Controller = "App";
        this.Action = "CheckV5";
        this.RunIt(sparams, new WebAPI.onWebAPIReturnListener() {
            public void onFinished(String result)
            {

                listener.onFinished((result));
            }

        });
    }


}
