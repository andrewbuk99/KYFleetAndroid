package com.mustardtec.yourfleet.util;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.mustardtec.yourfleet.DebugLog;
import com.mustardtec.yourfleet.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by danie on 28/02/2018.
 */

public class GovAPI extends AsyncTask<String, String, String> {

    private onGovAPIReturnListener mListerner;
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


    public GovAPI(Activity act, Context cont) {

        mAct = (MainActivity) act;
        mCont = cont;

    }

    public void RunIt(final String[] sParams, final onGovAPIReturnListener listener) {
        mListerner = listener;
        execute(sParams);
    }

    protected String doInBackground(String... vcall)
    {
        String p_s_regno = vcall[0];
        boolean FoundTax = false;
        HttpURLConnection urlConnection = null;
        String str ="https://vehicleenquiry.service.gov.uk/ConfirmVehicle";
        //str = "http://192.168.1.199:55990/testgov1.aspx";
        String strResponse = "";
        try {

            if ((p_s_regno == "enterregtoaddacar") || (p_s_regno.toLowerCase() == "enter reg to add a car")) {
                Invalid = true;
                return BuildResponse();
            }
            URL urlToRequest = new URL(str);
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
            List<Pair<String, String>> params = new ArrayList<>();
            params.add(new Pair<>("Vrm", p_s_regno.toUpperCase()));
    //        params.add(new Pair<>("password", password));
            String lData = getQuery(params);
            writer.write(lData);
            writer.flush();

            writer.close();
          //  out.write(getQuery(params));
            outNew.close();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
                Failed = true;
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
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
                result = new StringBuilder();
                urlConnection.disconnect();

                try {
                    if (result.toString().indexOf("ehicle details could not be found as it has not been possible") > -1)
                    {
                        NotFound = true;
                        return BuildResponse();
                    }
                    else if (result.toString().indexOf("must enter your registration number in a valid format") > -1)
                    {
                        Invalid = true;
                        return BuildResponse();
                    }
                    else if (result.toString().indexOf("aintenance") > -1)
                    {
                        try {
                            Document docTemp = Jsoup.parse(result.toString());
                            Elements inputTagsTemp = docTemp.select("input");
                            if (inputTagsTemp.size() == 0) {
                                SiteDown = true;
                                return BuildResponse();
                            } else {

                            }
                        }
                        catch (Exception ex) {
                            SiteDown = true;
                            return BuildResponse();
                        }
                    }

                    String bodyStr = "";
                    try {
                        int startpos = result.indexOf("<body");
                        int endpos = result.indexOf("</body>");
                        bodyStr = result.substring(startpos, endpos + 7);
                    }
                    catch (Exception ex)
                    {
                        bodyStr = result.toString();
                    }

                    Document doc  = Jsoup.parse(bodyStr);


                    Elements inputTags = doc.select("input");
                    if (inputTags != null) {
                        String VRM = "";
                        String l_viewstate = "";


                        for (Element tag : inputTags)
                        {
                            String lResult = tag.toString();
                            String id = tag.attr("id");
                            if (id.toLowerCase().equals("viewstate")) {
                                l_viewstate = tag.val();
                            }
                            if (id.toLowerCase().equals("vrm")) {
                                VRM = tag.val();
                            }
                            if (id.toLowerCase().equals("make")) {
                                Make = tag.val();
                            }
                            if (id.toLowerCase().equals("colour")) {
                                VehicleColour = tag.val();
                            }
                        }
                        if ((VRM != "") && (l_viewstate != "") && (Make != "") && (VehicleColour != ""))
                        {
                            try {
                               // Thread.sleep(300);
                                String str2 = "https://vehicleenquiry.service.gov.uk/ViewVehicle";
                                //str2 = "http://192.168.1.199:55990/testgov2.aspx";
                                URL urlToRequest2 = new URL(str2);
                                HttpURLConnection urlConnection2 = (HttpURLConnection)
                                        urlToRequest2.openConnection();
                                urlConnection2.setRequestMethod("POST");
                                urlConnection2.setUseCaches(false);
                                urlConnection2.setConnectTimeout(Timeout);
                                urlConnection2.setReadTimeout(Timeout);
                          //      urlConnection2.setRequestProperty("Content-Type", "text/html");
                                urlConnection2.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/64.0.3282.186 Safari/537.36");
                                BufferedOutputStream outNew2 = new BufferedOutputStream(urlConnection2.getOutputStream());
                                BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(outNew2, "UTF-8"));

                                params = new ArrayList<>();
                                params.add(new Pair<>("viewstate", l_viewstate));
                                params.add(new Pair<>("Vrm", VRM));
                                params.add(new Pair<>("Make", Make));
                                params.add(new Pair<>("Colour", VehicleColour));
                                params.add(new Pair<>("Correct", "True"));
                                lData = getQuery(params);

                                writer2.write(lData);
                                writer2.flush();

                                writer2.close();
                                outNew2.close();

                                statusCode = urlConnection2.getResponseCode();
                                if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                    // handle unauthorized (if service requires user login)
                                    Failed = true;
                                } else if (statusCode != HttpURLConnection.HTTP_OK) {
                                    SiteDown = true;
                                } else {
                                    InputStream in2 = new BufferedInputStream(
                                            urlConnection2.getInputStream());
                                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(in2));
                                    StringBuilder result2 = new StringBuilder();
                                    String line2;
                                    startwriting = false;
                                    while ((line2 = reader2.readLine()) != null) {
                                        if (line2.contains("<body"))
                                            startwriting = true;
                                        if (startwriting)
                                            result2.append(line2);
                                    }

                                    bodyStr = "";
                                    try {
                                        int startpos = result2.indexOf("<body");
                                        int endpos = result2.indexOf("</body>");
                                        bodyStr = result2.substring(startpos, endpos + 7);
                                    }
                                    catch (Exception ex)
                                    {
                                        bodyStr = result2.toString();
                                    }

                                    Document doc2 = Jsoup.parse(bodyStr);


                                    Elements inputTags2 = doc2.select("div.isValidMot");
                                    if (inputTags2.size() == 0) {
                                        inputTags2 = doc2.select("div.isValid");
                                    }
                                    if (inputTags2 != null) {
                                        for (Element tag : inputTags2) {
                                            Elements mainElements = tag.select("h2");
                                            if (mainElements.size() > 0) {
                                                Element innerTag = mainElements.get(0);
                                                String lHtml = innerTag.text().toLowerCase();

                                                if (lHtml.indexOf("tax") > -1) {
                                                        FoundTax = true;
                                                    String lHtmlDate = tag.select("p").text();
                                                    if (!lHtmlDate.isEmpty()) {
                                                        TaxExpiryDate = lHtmlDate.toLowerCase();
                                                        TaxExpiryDate = TaxExpiryDate.replace("<br>", "").replace("tax due:", "").replace("\"", "").trim();
                                                    }
                                                    Taxed = true;

                                                }
                                                else if (lHtml.indexOf("sorn") > -1)
                                                {
                                                    SORN = true;
                                                    FoundTax = true;

                                                }
                                                else if (lHtml.indexOf("mot") > -1) {
                                                    String lHtmlDate = tag.select("p").text();
                                                    if (!lHtmlDate.isEmpty()) {
                                                        MOTExpiryDate = lHtmlDate.toLowerCase();
                                                        MOTExpiryDate = MOTExpiryDate.replace("<br>", "").replace("expires:", "").replace("\"", "").trim();
                                                    }
                                                    MOTed = true;
                                                }

                                            }


                                        }
                                    }
                                    if (!FoundTax) {
                                        inputTags2 = doc2.select("div.isInvalid");
                                        if (inputTags2.size() > 0) {
                                            for (Element tag : inputTags2) {
                                                Elements mainElements = tag.select("h2");
                                                if (mainElements.size() > 0) {
                                                    Element taxTag = mainElements.get(0);
                                                    String lHtml = taxTag.text().toLowerCase();

                                                    if (lHtml.indexOf("tax") > -1) {
                                                        FoundTax = true;
                                                        String lHtmlDate = tag.select("p").text();
                                                        if (!lHtmlDate.isEmpty()) {
                                                            TaxExpiryDate = lHtmlDate.toLowerCase();
                                                            TaxExpiryDate = TaxExpiryDate.replace("<br>", "").replace("tax due:", "").replace("\"", "").trim();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Elements mainTags = doc2.select("li.list-summary-item");
                                    if (mainTags.size() > 0) {
                                        for (Element tag : mainTags)
                                        {
                                            try {
                                                String testStr = "";
                                                Elements spans = tag.select("span");
                                                try {

                                                    testStr = spans.get(0).html();
                                                } catch (Exception ex) {

                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("year of manufacture") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_date = l_s;
                                                            ModelYear = Integer.parseInt(l_s_date);
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("ate of first registration") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_date = l_s;
                                                            RegDate = "01 " + l_s_date;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("fuel type") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_fuel = l_s;
                                                            Fuel = l_s_fuel;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("cylinder capacity") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_engine = l_s.replace("<strong>", "").replace("</strong>", "").replace(" cc", "");;
                                                            Engine = l_s_engine;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("emissions") > -1 && testStr.toLowerCase().indexOf("rde") < 0) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_engine = l_s.replace("<strong>", "").replace("</strong>", "").replace(" cc", "");;
                                                            CO2 = l_s_engine;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("export marker") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_engine = l_s.replace("<strong>", "").replace("</strong>", "").replace(" cc", "");;
                                                            Export = l_s_engine;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("vehicle colour") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_engine = l_s.replace("<strong>", "").replace("</strong>", "").replace(" cc", "");;
                                                            VehicleColour = l_s_engine;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("vehicle type approval") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_engine = l_s.replace("<strong>", "").replace("</strong>", "").replace(" cc", "");;
                                                            VehicleTypeApproval = l_s_engine;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("wheelplan") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_engine = l_s.replace("<strong>", "").replace("</strong>", "").replace(" cc", "");;
                                                            WheelPan = l_s_engine;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                                try {
                                                    if (testStr.toLowerCase().indexOf("revenue weight") > -1) {
                                                        String l_s = spans.get(1).text();
                                                        if (!l_s.isEmpty()) {
                                                            String l_s_engine = l_s.replace("<strong>", "").replace("</strong>", "").replace(" cc", "");;
                                                            Weight = l_s_engine;
                                                        }
                                                    }
                                                }
                                                catch (Exception ex)
                                                {
                                                    ex.printStackTrace();
                                                }
                                            }
                                            catch (Exception ex)
                                            {
                                                ex.printStackTrace();
                                            }

                                        }
                                    } else {
                                        Failed = true;
                                    }
                                }

                            } catch (MalformedURLException e) {
                                Failed = true;
                                strResponse = "FAILED";
                                e.printStackTrace();
                            } catch (IOException e) {
                                Failed = true;
                                strResponse = "FAILED";
                                e.printStackTrace();
                            } catch (Exception ex) {
                                Failed = true;
                                strResponse = "FAILED";
                                ex.printStackTrace();
                            }


                        } else {
                            Failed = true;
                        }

                    }
                    else
                    {
                      Failed = true;

                    }

                }
                catch (Exception ex)
                {
                    Failed = true;

                }


            }

        } catch (MalformedURLException e) {
            Failed = true;
            strResponse = "FAILED";
            e.printStackTrace();
        } catch (IOException e) {
            SiteDown = true;
            strResponse = "FAILED";
            e.printStackTrace();
        } catch (Exception ex) {
            Failed = true;
            strResponse = "FAILED";
            ex.printStackTrace();
        }

        if ((!FoundTax) && (!SiteDown) && (!Invalid) && (!NotFound))
            Failed = true;

        return BuildResponse();
    }

    private String BuildResponse() {
        JSONObject jsonParam;
        jsonParam = new JSONObject();
        try {
            jsonParam.put("Failed", Failed);
            jsonParam.put("NotFound", NotFound);
            jsonParam.put("Invalid", Invalid);
            jsonParam.put("SiteDown", SiteDown);
            jsonParam.put("Taxed", Taxed);
            jsonParam.put("MOTed", MOTed);
            jsonParam.put("SORN", SORN);
            jsonParam.put("Make", Make);
            jsonParam.put("MOTExpiryDate", MOTExpiryDate);
            jsonParam.put("TaxExpiryDate", TaxExpiryDate);
            jsonParam.put("ModelYear", ModelYear);
            jsonParam.put("RegDate", RegDate);
            jsonParam.put("Fuel", Fuel);
            jsonParam.put("Engine", Engine);
            jsonParam.put("CO2", CO2);
            jsonParam.put("Export", Export);
            jsonParam.put("VehicleColour", VehicleColour);
            jsonParam.put("VehicleTypeApproval", VehicleTypeApproval);
            jsonParam.put("WheelPan", WheelPan);
            jsonParam.put("Weight", Weight);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonParam.toString();
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

    public interface onGovAPIReturnListener {
        /**
         * Called to notify that setup is complete.
         *
         * @param result The result of the setup process.
         */
        void onFinished(String result);
    }
}
