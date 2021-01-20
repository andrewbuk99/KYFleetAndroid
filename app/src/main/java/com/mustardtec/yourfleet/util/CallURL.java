package com.mustardtec.yourfleet.util;

/**
 * Created by danie on 26/03/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.mustardtec.yourfleet.DebugLog;
import com.mustardtec.yourfleet.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class CallURL extends AsyncTask<String, String, String>  {
    private onCallURLAPIReturnListener mListerner;
    private MainActivity mAct;
    private Context mCont;
    DebugLog errLog = null;
    public Integer Timeout = 20000;
    public  boolean Failed = false;
    public boolean NotFound = false;
    public boolean Invalid = false;
    public boolean SiteDown = false;
    public boolean Taxed = false;
    public boolean MOTed = false;
    public boolean SORN = false;
    public String Make = "";
    public String MOTExpiryDate = "";
    public String TaxExpiryDate = "";
    public int ModelYear = 0;
    public String RegDate = "";
    public String Fuel = "";
    public String Engine = "";
    public String CO2 = "";
    public String Export = "";
    public String VehicleColour = "";
    public String VehicleTypeApproval = "";
    public String WheelPan = "";
    public String Weight = "";

    public CallURL(Activity act, Context cont) {

        mAct = (MainActivity) act;
        mCont = cont;

    }
    public void RunIt(final String[] sParams, final onCallURLAPIReturnListener listener) {
        mListerner = listener;
        execute(sParams);
    }

    protected String doInBackground(String... vcall)
    {
        String PayLoad = vcall[0];
        String pUrl = vcall[1];
        String pLabel = vcall[2];
        boolean FoundTax = false;
        HttpURLConnection urlConnection = null;
        //str = "http://192.168.1.199:55990/testgov1.aspx";
        String strResponse = "";
        try {

            URL urlToRequest = new URL(pUrl);
            urlConnection = (HttpURLConnection)
                    urlToRequest.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(Timeout);
            urlConnection.setReadTimeout(Timeout);
            //      urlConnection.setRequestProperty("Content-Type", "text/html");
            urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/64.0.3282.186 Safari/537.36");
            //    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            OutputStream outNew;
            outNew = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outNew, "UTF-8"));
            writer.write(PayLoad);
            writer.flush();

            writer.close();
            //  out.write(getQuery(params));
            outNew.close();
            String retVal = "";
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
                strResponse = "error:" + statusCode;
                Failed = true;
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                strResponse = "error:" + statusCode;
                SiteDown = true;
            } else {
                InputStream in = new BufferedInputStream(
                        urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                boolean startwriting = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("<body"))
                        startwriting = true;
                    if (startwriting)
                        result.append(line);
                }
                reader.close();
                urlConnection.disconnect();

                strResponse = result.toString().replace("'","`");

            }

        } catch (MalformedURLException e) {
            Failed = true;
            strResponse = "error:mailformedurl";
            e.printStackTrace();
        } catch (IOException e) {
            SiteDown = true;
            strResponse = "error:sitedown";
            e.printStackTrace();
        } catch (Exception ex) {
            Failed = true;
            strResponse = "error:failed";
            ex.printStackTrace();
        }

        return strResponse;
    }


    private String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException
    {


        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Pair<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));

        }

        return result.toString();
    }
    @Override
    protected void onPostExecute(String result) {
        mListerner.onFinished(result);
    }

    public interface onCallURLAPIReturnListener {
        /**
         * Called to notify that setup is complete.
         *
         * @param result The result of the setup process.
         */
        void onFinished(String result);
    }
}

