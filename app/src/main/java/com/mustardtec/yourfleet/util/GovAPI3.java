package com.mustardtec.yourfleet.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mustardtec.yourfleet.DebugLog;
import com.mustardtec.yourfleet.HtmlAPI;
import com.mustardtec.yourfleet.MainActivity;
import com.mustardtec.yourfleet.appGlobal;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class GovAPI3 {

    private MainActivity mAct;
    private Context mCont;
    DebugLog errLog = null;
    public Integer Timeout = 20000;
    public boolean Failed = false;
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
    public HtmlAPI mHtmlAPI;
    private Handler mCheckStatus;
    private CookieManager mCookieManager = null;
    private WebView mWebView2 = null;
    private int mNoCalls = 0;
    private String mReturnFunc = "";
    private WaitForHTML mWaitForHTML;
    private String mRegNo = "";
    private String mForceRefresh = "";

    public GovAPI3(Activity act, Context cont, WebView webView, HtmlAPI htmlAPI) {

        mAct = (MainActivity) act;
        mCont = cont;
        mWebView2 = webView;
        mHtmlAPI = htmlAPI;

    }

    public void RunIt(final String[] sParams) {

        final String RegNo = sParams[0];
        final String forceRefresh = sParams[1];
        final String pReturnFunc = sParams[2];
        mRegNo = RegNo;

        mForceRefresh = forceRefresh;
        mReturnFunc = pReturnFunc;

        mWebView2.getSettings().setJavaScriptEnabled(true);
        this.mCookieManager = android.webkit.CookieManager.getInstance();
        this.mCookieManager.setAcceptThirdPartyCookies(mWebView2, true);
        this.mCookieManager.removeSessionCookies(null);
        final MyJavaScriptInterface jInterface = new MyJavaScriptInterface(mCont);
        mWebView2.addJavascriptInterface(jInterface, "HtmlViewer");
        mNoCalls = 0;

        mWebView2.setWebViewClient(new WebViewClient() {
            boolean timeout;
            boolean webViewError;
            public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                timeout = true;
                webViewError = false;
                Runnable run = new Runnable() {
                    public void run() {
                        if(timeout)
                        {
                            mHtmlAPI.CallUrlRtn("error:2", "Gov2", forceRefresh, RegNo,mReturnFunc);
                        }
                    }
                };
                Looper myLoop = mCont.getMainLooper();
                Handler myHandler = new Handler(myLoop.myLooper());
                myHandler.postDelayed(run, 60000);


            }
            @Override
            public void onPageFinished(WebView view, String url) {
                timeout = false;
                StringBuilder sb = new StringBuilder();

                if (RegNo != "") {

                    sb.append("function kcheck() {");
                    sb.append("try {");
                    // sb.append("	window.addEventListener('load', function () {");
                    sb.append("	var shtml = document.getElementsByTagName('body')[0].innerHTML;");
                    sb.append("	if (shtml.indexOf('wizard_vehicle_enquiry_capture_vrn_vrn') > -1) { window.HtmlViewer.showHTML('<kyc>' + shtml + '</kyc>'); }");
                    sb.append("	else if (shtml.indexOf('yes-vehicle-confirm') > -1) { window.HtmlViewer.showHTML('<kyc>'+shtml+'</kyc>');}");
                    sb.append("	else if (shtml.indexOf('reg-mark') > -1) { window.HtmlViewer.showHTML('<kyc>'+shtml+'</kyc>');}");
                    sb.append("	else if (shtml.indexOf('ehicle details could not be found') > -1 || shtml.indexOf('registration number you have provided is invalid') > -1 || shtml.indexOf('aintenance') > -1) { window.HtmlViewer.showHTML('<kyc>'+shtml+'</kyc>');}");
                    sb.append("	else { setTimeout(function () { kcheck(); }, 100);}");
                    //  sb.append("	}); ");
                    sb.append("}");
                    sb.append("catch (err) { setTimeout(function () { kcheck(); }, 100); }");
                    sb.append("}");
                    sb.append("setTimeout(function () { kcheck(); }, 100);");

                    mWebView2.loadUrl("javascript:" + sb.toString());

                    if (mCheckStatus != null) {
                        try {
                           mCheckStatus.removeCallbacksAndMessages(null);
                        }
                        catch (Exception ex) {}
                    }
                    mCheckStatus = new Handler();
                    mCheckStatus.postDelayed(
                            new Runnable() {
                                public void run() {
                                        mHtmlAPI.CallUrlRtn("error:2", "Gov2", forceRefresh, RegNo, mReturnFunc);

                                }
                            }, 40000);

                }

            }

            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                mAct.debugLog.e("ERROR:", "Android: GovAPI2 onReceivedError:" + error.toString() + " URL:" + request.getUrl());


            }

            public void onReceivedHttpError(WebView view,
                                            WebResourceRequest request, WebResourceResponse errorResponse) {
                mAct.debugLog.e("ERROR:", "Android: GovAPI2 onReceivedHttpError:" + errorResponse.getReasonPhrase() + " URL:" + request.getUrl());

                super.onReceivedHttpError(view, request, errorResponse);

            }
        });

        mWebView2.loadUrl("https://vehicleenquiry.service.gov.uk/");

    }

    public class MyJavaScriptInterface {

        private Context ctx;
        public String html;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        public String getHtml()
        {
            int actualgoes = 0;
            boolean vfound = false;
            long startTime = System.currentTimeMillis();
            try {
                do {
                    actualgoes++;
                    if (html != null) {
                        actualgoes=999999;
                        vfound=true;
                    }
                } while (((System.currentTimeMillis()-startTime)<10000) && !vfound);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return html;

        }

        public void ProcessHTML(String result) {
            boolean okToGo = true;
            if (result == null || result.isEmpty()) {
                okToGo = false;
            } else {
                try {
                    if (result.toString().indexOf("ehicle details could not be found") > -1) {
                        NotFound = true;
                        okToGo = false;

                    } else if (result.toString().indexOf("registration number you have provided is invalid") > -1) {
                        Invalid = true;
                        okToGo = false;

                    } else if (result.toString().indexOf("aintenance") > -1) {
                        try {
                            Document docTemp = Jsoup.parse(result.toString());
                            Elements inputTagsTemp = docTemp.select("input");
                            if (inputTagsTemp.size() == 0) {
                                SiteDown = true;
                                okToGo = false;
                            } else {

                            }
                        } catch (Exception ex) {
                            SiteDown = true;
                            okToGo = false;
                        }
                    }

                } catch (Exception ex) {
                    mAct.debugLog.e("ERROR:", "Android: Gov2API reading initial html:" + ex.getMessage());
                }
            }
            if (okToGo) {
                if (result.indexOf("Vehicle make") > -1) {
                    try {
                        if (mCheckStatus != null) {
                            mCheckStatus.removeCallbacksAndMessages(null);
                        }
                    }
                    catch (Exception ex) {}
                    result = result.toString().replace("'", "`");
                    mHtmlAPI.CallUrlRtn(result, "Gov2", mForceRefresh, mRegNo, mReturnFunc);
                } else {

                    if (result.indexOf("wizard_vehicle_enquiry_capture_vrn_vrn") > -1) {
                        mWebView2.post(new Runnable() {
                            @Override
                            public void run() {
                                String lJs = "";
                                try {
                                    lJs = ((appGlobal) mAct.getApplication()).getGovStr();
                                } catch (Exception ex) {
                                }
                                if (lJs != "") {
                                    mWebView2.loadUrl(lJs);
                                } else {
                                    mWebView2.loadUrl("javascript:document.getElementById('wizard_vehicle_enquiry_capture_vrn_vrn').value='" + mRegNo + "';document.getElementById('submit_vrn_button').click();");
                                    //  mWebView2.loadUrl("javascript:$('#Correct_True').prop('checked', true);$('button[name=Continue]').click();");
                                }
                            }
                        });
                    } else {
                        if (result.indexOf("yes-vehicle-confirm") > -1 && result.indexOf("authenticity_token") > -1) {
                            try {
                                String[] array1 = result.split("authenticity_token");
                                final String[] array2 = array1[1].split("\"");


                                mWebView2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String lJs = "";
                                        try {
                                            lJs = ((appGlobal) mAct.getApplication()).getGovStr();
                                        } catch (Exception ex) {
                                        }
                                        if (lJs != "") {
                                            mWebView2.loadUrl(lJs);
                                        } else {
                                            String url =  "https://vehicleenquiry.service.gov.uk/VehicleFound?locale=en&utf8=&authenticity_token=" + array2[2] + "&wizard_vehicle_enquiry_capture_confirm_vehicle[confirmed]=Yes";
                                            mWebView2.loadUrl(url);
                                           // mWebView2.loadUrl("javascript:document.getElementById('yes-vehicle-confirm').checked = true;document.getElementById('capture_confirm_button').click();");
                                            //  mWebView2.loadUrl("javascript:$('#Correct_True').prop('checked', true);$('button[name=Continue]').click();");
                                        }
                                    }
                                });
                            }
                            catch (Exception ex) {
                                mHtmlAPI.CallUrlRtn("error:1b", "Gov1", mForceRefresh, mRegNo, mReturnFunc);
                            }
                        }
                    }


                }

            } else {
                //  mAct.debugLog.DebugSendLog("ERROR:", "Android: GovAPI2 error4: not ok");
                result = result.toString().replace("'", "`");
                try {
                    if (mCheckStatus != null) {
                        mCheckStatus.removeCallbacksAndMessages(null);
                    }
                }
                catch (Exception ex) {}
                mHtmlAPI.CallUrlRtn(result, "Gov1", mForceRefresh, mRegNo, mReturnFunc);
            }
        }

        @JavascriptInterface
        public void showHTML(String _html) {

            html = _html;
            //   Log.d("ANDROID: GovAPI2", "appReady html: " + html);
            ((appGlobal) mAct.getApplication()).setHtmlStr(_html);
            ProcessHTML(_html);
        }

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
}
