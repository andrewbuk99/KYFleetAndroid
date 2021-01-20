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

/**
 * Created by danie on 28/02/2018.
 */

public class GovAPI2 {

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
    private CookieManager mCookieManager = null;
    private WebView mWebView2 = null;
    private int mNoCalls = 0;
    private String mReturnFunc = "";
    private WaitForHTML mWaitForHTML;
    private boolean IsDone = false;

    public GovAPI2(Activity act, Context cont, WebView webView, HtmlAPI htmlAPI, String pReturnFunc) {

        mAct = (MainActivity) act;
        mCont = cont;
        mWebView2 = webView;
        mHtmlAPI = htmlAPI;
        mReturnFunc = pReturnFunc;


    }

    public void RunIt(final String[] sParams) {

        final String RegNo = sParams[0];
        final String forceRefresh = sParams[1];
        mWebView2.getSettings().setJavaScriptEnabled(true);
        this.mCookieManager = android.webkit.CookieManager.getInstance();
        this.mCookieManager.setAcceptThirdPartyCookies(mWebView2, true);
        this.mCookieManager.removeSessionCookies(null);
        final MyJavaScriptInterface jInterface = new MyJavaScriptInterface(mCont);
        mWebView2.addJavascriptInterface(jInterface, "HtmlViewer");
        mNoCalls = 0;

        //final WaitForHTML waitForHTML = new WaitForHTML(mAct, mCont);

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
                myHandler.postDelayed(run, 15000);

            }
            @Override
            public void onPageFinished(WebView view, String url) {
                timeout = false;
                StringBuilder sb = new StringBuilder();
//                sb.append("function kcheck() {");
////                sb.append("try {");
////                sb.append(" window.onload = function () {");
////                sb.append("   var shtml = document.getElementsByTagName('body')[0].innerHTML;");
////                sb.append("   if ($('#viewstate').length > 0) {");
////                sb.append("    window.HtmlViewer.showHTML('<kyc>'+shtml+'</kyc>');");
////                sb.append("   }");
////                sb.append("   else if ((shtml.includes('ehicle details could not be found as it has not been possible')) || (shtml.includes('must enter your registration number in a valid format')) || (shtml.includes('aintenance')))");
////                sb.append("   {");
////                sb.append("    window.HtmlViewer.showHTML('<kyc>'+shtml+'</kyc>');");
////                sb.append("   }");
////                sb.append("   else {");
////                sb.append("    setTimeout(function () { kcheck(); }, 100);");
////                sb.append("   }");
////                sb.append("  });");
////                sb.append(" } else {");
////                sb.append("   setTimeout(function () { kcheck(); }, 100);");
////                sb.append(" }");
////                sb.append("}");
////                sb.append(" catch (err) { }");
////                sb.append("}");
////                sb.append("setTimeout(function () { kcheck(); },100);");
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
                try {
                    mWaitForHTML.Cancel();
                }
                catch (Exception wx) {

                }
                if (IsDone) return; // prevent any further runs as we've completed this run

                    mWaitForHTML = new WaitForHTML(mAct, mCont);
                    mWaitForHTML.RunIt(new WaitForHTML.onReturnHTMLListener() {
                        @Override
                        public void onFinished(String res) {
                            try {
                                if (res != "OK") {

                                    mAct.debugLog.e("ERROR:", "Android: GovAPI2 error5: not ok");
                                    mHtmlAPI.CallUrlRtn("error:5", "Gov2", forceRefresh, RegNo, mReturnFunc);
                                    IsDone = true;
                                    //  dialog.setMessage("Failed to contact server");
                                    //    System.exit(0);
                                } else {
                                    mNoCalls++;
                                    if (mNoCalls > 4) {
                                        IsDone = true;
                                        mHtmlAPI.CallUrlRtn("error:1", "Gov2", forceRefresh, RegNo, mReturnFunc);
                                    }
                                    else {
                                        String result = jInterface.html;
                                        ((appGlobal) mAct.getApplication()).setHtmlStr("");
                                        boolean okToGo = true;
                                        if (result == null || result.isEmpty()) {
                                            okToGo = false;
                                            timeout = true;
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
                                                result = result.toString().replace("'", "`");
                                                IsDone = true;
                                                mHtmlAPI.CallUrlRtn(result, "Gov2", forceRefresh, RegNo, mReturnFunc);
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
                                                                mWebView2.loadUrl("javascript:document.getElementById('wizard_vehicle_enquiry_capture_vrn_vrn').value='" + RegNo + "';document.getElementById('submit_vrn_button').click();");
                                                                //  mWebView2.loadUrl("javascript:$('#Correct_True').prop('checked', true);$('button[name=Continue]').click();");
                                                            }
                                                        }
                                                    });
                                                } else {
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
                                                                mWebView2.loadUrl("javascript:document.getElementById('yes-vehicle-confirm').checked = true;document.getElementById('capture_confirm_button').click();");
                                                                //  mWebView2.loadUrl("javascript:$('#Correct_True').prop('checked', true);$('button[name=Continue]').click();");
                                                            }
                                                        }
                                                    });
                                                }


                                            }

                                        } else {
                                            //  mAct.debugLog.DebugSendLog("ERROR:", "Android: GovAPI2 error4: not ok");
                                            result = result.toString().replace("'", "`");
                                            IsDone = true;
                                            mHtmlAPI.CallUrlRtn(result, "Gov1", forceRefresh, RegNo, mReturnFunc);
                                        }
                                    }

                                }
                            } catch (Exception ex) {
                                mAct.debugLog.e("ERROR:", "Android: GovAPI2 error:" + ex.getMessage());
                                mHtmlAPI.CallUrlRtn("error:3", "Gov2", forceRefresh, RegNo, mReturnFunc);
                            }
                        }
                    });



            }

            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                // mAct.debugLog.DebugSendLog("ERROR: ANDROID:" , "HtmlAPI onReceivedError:" + error.toString() + " URL:" + request.toString());
                mAct.debugLog.e("ERROR:", "Android: GovAPI2 onReceivedError:" + error.toString() + " URL:" + request.getUrl());


            }

            public void onReceivedHttpError(WebView view,
                                            WebResourceRequest request, WebResourceResponse errorResponse) {
                //   mAct.debugLog.d("ERROR: ANDROID:" , "HtmlAPI onReceivedHttpError:" + errorResponse.toString() + " URL:" + request.toString());
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

        @JavascriptInterface
        public void showHTML(String _html) {

            html = _html;
         //   Log.d("ANDROID: GovAPI2", "appReady html: " + html);
            ((appGlobal) mAct.getApplication()).setHtmlStr(_html);
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

