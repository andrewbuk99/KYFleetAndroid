package com.mustardtec.yourfleet;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import java.io.File;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.os.Build;
import android.content.ClipboardManager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.ShareDialog;
import com.mustardtec.yourfleet.util.CallURL;
import com.mustardtec.yourfleet.util.GovAPI;
import com.mustardtec.yourfleet.util.GovAPI3;
import com.mustardtec.yourfleet.util.MustardPurchases;
//import com.mustardtec.yourfleet.util.WaitForFrontEnd;


import java.net.URLEncoder;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dan on 22/09/2017.
 */


public class HtmlAPI {
    private Context mCont;
    private WebView mWebView = null;
    private WebView mWebView2 = null;
    private CookieManager mCookieManager = null;
    private String mAndroidId = "";
    private String mAuthToken = "";
    private String mAppMode = "";
    private MainActivity mAct;
    private String mURL = "";


    public HtmlAPI(MainActivity pAct, WebView pWebView, WebView pWebView2, Context cont, CookieManager pCookieManager) {
        mWebView = pWebView;
        mWebView2 = pWebView2;
        mCookieManager = pCookieManager;
        mAct = pAct;
        mAndroidId =  ((appGlobal) mAct.getApplication()).getAppDeviceId();
        mAuthToken = ((appGlobal) mAct.getApplication()).getAuthToken();
        mCont = cont;
        String referrer = ((appGlobal) mAct.getApplication()).getAppReferrer();
        if (!referrer.startsWith("utm"))
            referrer = "";

//        mGovtest = new GovAPI3(mAct,mCont, mWebView2, this);
//        mWebView.post(new Runnable() {
//            @Override
//            public void run() {
//
//                String[] sparams = {"", "", ""};
//                ((appGlobal) mAct.getApplication()).setHtmlRunningStatus(true);
//                mGovtest.RunIt(sparams);
//            }
//        });

        if (BuildConfig.DEBUG) {
            mURL = mAct.getResources().getString(R.string.mainDomain_debug) + "v" + ((appGlobal)mAct.getApplicationContext()).getAppVersion() + "/index.html?platform=android";
            if (!referrer.equals("")) mURL += "&" + referrer;

        } else {
            mURL = mAct.getResources().getString(R.string.mainDomain) + "v" + ((appGlobal)mAct.getApplicationContext()).getAppVersion() + "/index.html?platform=android";
            if (!referrer.equals("")) mURL += "&" + referrer;
        }

        mWebView.setWebViewClient(new WebViewClient() {
            boolean timeout;
            boolean webViewError;

            @Override
            public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                timeout = true;
                webViewError = false;
                Runnable run = new Runnable() {
                    public void run() {
                        if(timeout) {
                            if (url.startsWith(mURL)) {
                                //ClearHTML5();
                                mWebView.loadUrl("file:///android_asset/reloadandroid.html");
                            }

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
                mAct.findViewById(R.id.webViewLoading).setVisibility(View.GONE);
                //show webview
                mAct.findViewById(R.id.webView).setVisibility(View.VISIBLE);

//                if (!webViewError) {
//                    if (url.startsWith(mURL)) {
//                        WaitForFrontEnd waitForFrontEnd = new WaitForFrontEnd(mAct, mCont);
//                        waitForFrontEnd.RunIt(new WaitForFrontEnd.onReturnListener() {
//                            @Override
//                            public void onFinished(String result) {
//                                if (result != "OK") {
//                                    mAct.debugLog.d("ERROR: ANDROID:", "HtmlAPI waitForFrontEnd result: not ok ");
//                                    mAct.htmlAPI.showError("Failed to contact server");
//                                    //  dialog.setMessage("Failed to contact server");
//                                    //    System.exit(0);
//                                } else {
//
//                                    if (!mAct.NotificationPayLoad.equals("")) {
//                                        mAct.htmlAPI.NotificationDataRtn(mAct.NotificationPayLoad);
//                                    }
//
//                                    try {
//                                        Uri data = mAct.getIntent().getData();
//                                        String scheme = data.getScheme(); // "http"
//                                        if (scheme.equals("https")) {
//                                            String lquery = data.getQuery();
//                                            if (!lquery.isEmpty()) {
//                                                mAct.htmlAPI.NotificationDataRtn(lquery.replace("data=",""));
//                                            }
//                                            mAct.getIntent().setData(null);
//                                        }
//                                    }
//                                    catch (Exception ex)
//                                    {
//
//                                    }
//
//                                }
//                            }
//                        });
//                    }
//                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){

               // mAct.debugLog.DebugSendLog("ERROR: ANDROID:" , "HtmlAPI onReceivedError:" + error.toString() + " URL:" + request.toString());
                mAct.debugLog.e("ERROR:" , "Android: HtmlAPI onReceivedError:" + error.toString() + " URL:" + request.getUrl());

                String appReady = ((appGlobal) mAct.getApplication()).getFrontEndReady();
                if ((appReady == "" || appReady == null) && (request.getUrl().toString().startsWith(mURL))) {
                    webViewError = true;
                    mWebView.loadUrl("file:///android_asset/reloadandroid.html");
                    mAct.findViewById(R.id.webViewLoading).setVisibility(View.GONE);
                    mAct.findViewById(R.id.webView).setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onReceivedHttpError(WebView view,
                                            WebResourceRequest request, WebResourceResponse errorResponse) {
             //   mAct.debugLog.d("ERROR: ANDROID:" , "HtmlAPI onReceivedHttpError:" + errorResponse.toString() + " URL:" + request.toString());
               mAct.debugLog.e("ERROR:" , "Android: HtmlAPI onReceivedHttpError:" + errorResponse.getData() + " URL:" + request.getUrl());
                String appReady = ((appGlobal) mAct.getApplication()).getFrontEndReady();
                if ((appReady == "" || appReady == null) && (request.getUrl().toString().startsWith(mURL))) {
                    webViewError = true;
                    mWebView.loadUrl("file:///android_asset/reloadandroid.html");
                    mAct.findViewById(R.id.webViewLoading).setVisibility(View.GONE);
                    mAct.findViewById(R.id.webView).setVisibility(View.VISIBLE);
                }

                super.onReceivedHttpError(view, request, errorResponse);

            }
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return OverRideURL(url);
            }

            @RequiresApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                return OverRideURL(url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg)
            {
                //String url = view.getUrl().toString();
                WebView.HitTestResult result = view.getHitTestResult();
                int type = result.getType();
                String data = result.getExtra();
                Message href = view.getHandler().obtainMessage();
                view.requestFocusNodeHref(href);
                android.os.Bundle src = href.getData();
                android.os.Bundle src2 = resultMsg.getData();
                for (String key: src.keySet())
                {
                    Log.d ("myApplication", key + " is a key in the bundle");
                    Log.d ("myApplication", src.getString(key )+ " is a value in the bundle");
                }
                for (String key: src2.keySet())
                {
                    Log.d ("myApplication2", key + " is a key in the bundle");
                    Log.d ("myApplication2", src2.getString(key)+ " is a value in the bundle");
                }
                String url = href.getData().getString("url");
                if (url != null)
                    return OverRideURL(url);
                else {
                    WebView newWebView = new WebView(mCont);
                    newWebView.getSettings().setJavaScriptEnabled(true);
                    newWebView.getSettings().setSupportZoom(true);
                    newWebView.getSettings().setBuiltInZoomControls(true);
                    newWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                    newWebView.getSettings().setSupportMultipleWindows(true);
                    //view.addView(newWebView);
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(newWebView);
                    resultMsg.sendToTarget();

                    newWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {

                            return OverRideURL(url);
                        }
                    });

                    return true;
                }
            }
        });
       // mWebView.setWebChromeClient(new wecrome());
    }

    public Boolean OverRideURL(String url) {
        if( url.startsWith("http:") || url.startsWith("https:") )
        {
            Uri marketUri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
            try {
                mAct.startActivity(intent);
            }
            catch (Exception ex) {
                mAct.debugLog.d("ERROR: ANDROID:", "OverRideURL http err:" + ex.getMessage());
            }
            return false;
        }

        // Otherwise allow the OS to handle it
        else if (url.startsWith("tel:")) {
            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            try {
                mAct.startActivity(tel);
            }
            catch (Exception ex) {
                mAct.debugLog.d("ERROR: ANDROID:", "OverRideURL tel err:" + ex.getMessage());
            }
            return true;
        }
//                else if (url.startsWith("mailto:")) {
//
//                    try {
//                        String body = "Enter your Question, Enquiry or Feedback below:\n\n";
//
//                        Intent mail = new Intent(Intent.ACTION_SENDTO);
//                        String lEmail = url.toString().replace("mailto:", "");
//                        String lUserId = ((appGlobal) mCont.getApplicationContext()).getAppUserID();
//                        mail.setType("message/rfc822");
//                        mail.setData(Uri.parse("mailto:" + lEmail));
//                        mail.putExtra(Intent.EXTRA_EMAIL, new String[]{lEmail});
//                        mail.putExtra(Intent.EXTRA_SUBJECT, "Enquiry from User:" + lUserId + " - Know YourCar");
//                        mail.putExtra(Intent.EXTRA_TEXT, body);
//                        mail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mail.addFlags(Intent.FLAG_FROM_BACKGROUND);
//                        mAct.startActivity(mail);
//                    } catch (Exception ex) {
//                        showError("Sorry we can't connect to your email app");
//                    }
//                    return true;
//                }
        else if ((url.startsWith("market:")) || (url.startsWith("mailto:")) || (url.startsWith("https://twitter.com:")))
        {
            Uri marketUri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
            try {
                mAct.startActivity(intent);
            }
            catch (Exception ex) {
                mAct.debugLog.d("ERROR: ANDROID:", "OverRideURL market err:" + ex.getMessage());
            }
        }
        else if (url.startsWith("whatsapp:"))
        {
            try {
                mCont.getPackageManager().getPackageInfo("com.whatsapp", 0);
                Uri marketUri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
                mAct.startActivity(intent);
            }
            catch (Exception ex) {
                Uri marketUri = Uri.parse("market://details?id=com.whatsapp");
                Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
                try {
                    mAct.startActivity(intent);
                }
                catch (Exception ex2) {
                    mAct.debugLog.d("ERROR: ANDROID:", "OverRideURL whatsapp err:" + ex2.getMessage());
                }
            }



        }
        else if (url.startsWith("fb:"))
        {
            try {
                mCont.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                Uri marketUri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
                mAct.startActivity(intent);
            } catch (Exception e) {
                String lUrl = url.replace("fb://","https://www.facebook.com/");
                Uri marketUri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
                try {
                    mAct.startActivity(intent);
                }
                catch (Exception ex2) {
                    mAct.debugLog.d("ERROR: ANDROID:", "OverRideURL facebook err:" + ex2.getMessage());
                }
            }

        }
        return true;
    }

    public void SetUserID(String p_s_userid)
    {
        mAndroidId =  ((appGlobal) mAct.getApplication()).getAppDeviceId();
        mAuthToken = ((appGlobal) mAct.getApplication()).getAuthToken();
        mAppMode = ((appGlobal) mAct.getApplication()).getAppMode();
  //      PwStorage hashing = new PwStorage();
        final String lUserId = p_s_userid;
  //      final String deviceIdToken = hashing.main(mAndroidId);
        final String deviceIdToken ="";
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:setUserid('" + lUserId + "','"+mAndroidId+"','" + mAuthToken + "','" + deviceIdToken + "','" + mAppMode + "');");
            }
        });

    }
    public void BraintreeRtn(final String pStatus, final String pNonce, final String pError, final String pProduct, final String pSku, final String pOriginalTask)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:BraintreeRtn('" + pStatus + "','" + pNonce + "','" + pError + "','" + pProduct + "','" + pSku + "','" + pOriginalTask + "');");
            }
        });
    }
    public void PostLoginRtn()
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:PostLoginRtn();");
            }
        });
    }
    public void AppCameintoForeGround()
    {
        try {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:AppCameintoForeGround('android');");
                }
            });
        }
        catch (Exception ex) {

        }
    }
    public void ShowPremium()
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:showPremiumRtn();");
            }
        });
    }
    public void JWTrtn(final String pToken)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:JWTrtn('" + pToken + "');");
            }
        });
    }
    public void WriteLog(final String pTag, final String pMess)
    {
        try {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:WriteLog('" + pTag + "','" + pMess + "');");
                }
            });
        }
        catch (Exception ex) {}
    }
    public void PauseFrontEnd()
    {
        try {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:PauseFrontEnd();");
                }
            });
        }
        catch (Exception ex) {}
    }

    public void setAndroidSubVersionRtn()
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                String lSub = "1";
                try {
                    lSub = ((appGlobal) mCont.getApplicationContext()).getAppSubVersion();
                }
                catch (Exception ex) { lSub = "1";}
                mWebView.loadUrl("javascript:setAndroidSubVersionRtn(" + lSub + ");");
            }
        });
    }

    public void NotificationDataRtn(final String pdata)
    {
        try {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mWebView.loadUrl("javascript:NotificationDataRtn('" + URLEncoder.encode(pdata, "UTF-8") + "');");
                    }
                    catch (Exception ex)
                    {

                    }
                }
            });
        }
        catch (Exception ex) {

        }
    }
    public void showError(final String pMessage)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:showAppError('" + pMessage + "');");
            }
        });

    }
    public void showNotification(final String pData, final String pTitle, final String pBody)
    {
        final String lTitle = pTitle.replace("'","&#39;");
        final String lBody = pBody.replace("'","&#39;");

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:showNotification('" + lTitle + "','" + lBody + "', '" + pData + "');");
            }
        });
    }
    public void showLoading()
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:showLoading('');");
            }
        });
    }
    public void hideLoading()
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:hideLoading();");
            }
        });
    }

    public void doBillingRtn(final String pResult, final String pProduct, final String pFunctionToCall, final String pParams)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:doBillingRtn('" + pResult + "','" + pProduct + "','" + pFunctionToCall + "','" + pParams + "');");
            }
        });
    }
    public void doRewardVideoRtn(final String pResult)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:doRewardVideoRtn('" + pResult + "');");
            }
        });
    }
    public void doRestoreRtn(final String pResult)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:doRestoreRtn('" + pResult + "');");
            }
        });

    }
    public void doRestoreRtn(final String pResult, final String pFunctionToCall)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:doRestoreRtn('" + pResult + "','" + pFunctionToCall + "');");
            }
        });
    }
    public void fbInviteRtn(final String pResult)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:fbInviteRtn('" + pResult + "');");
            }
        });
    }
    public void fbLogoutRtn(final String pID, final String pName)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:fbLogoutRtn('" + pID + "','" + pName + "');");
            }
        });
    }
    public void fbLoginRtn(final String pID, final String pName, final String pAccessToken, final String pEmail)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:fbLoginRtn('" + pID + "','" + pName + "','" + pAccessToken + "','" + pEmail + "');");
            }
        });
    }
    public void googleLoginRtn(final String pIdToken, final String pHash)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:googleLoginRtn('" + pIdToken + "','" + pHash + "');");
            }
        });
    }
    public void sendReferrer(final String pReferrer)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:sendReferrer('" + pReferrer + "');");
            }
        });
    }
    @JavascriptInterface
    public void LoadMainScreen(boolean reLoad)
    {
        try {
            String sUrl = mURL;
            if (reLoad)
            {
                sUrl += "&reload=" + System.currentTimeMillis();
            }
            final String lUrl = sUrl;
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    ((appGlobal) mAct.getApplication()).setFrontEndDone(false);
                    mWebView.loadUrl(lUrl);
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    mAct.mCookieManager.flush();
                                    mAct.debugLog.d("INFO: ANDROID:", "LoadMainScreen Cookies sync");
                                }
                            },
                            5000);
                }
            });
        }
        catch (Exception ex)
        {

        }

    }
    public void LoadMaintenanceScreen()
    {
        try {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG) {
                        mWebView.loadUrl(mAct.getResources().getString(R.string.maintenanceUrl_debug));
                    } else {
                        mWebView.loadUrl(mAct.getResources().getString(R.string.maintenanceUrl));
                    }
                }
            });


        }
        catch (Exception ex)
        {

        }

    }

    public void SaveUrl(final String pUrl, final String pType)
    {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:doSaveURL('" + pUrl + "','" + pType + "');");
            }
        });
    }
    public void UpdatingScreen() {

        try {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:try {showUpdatingScreen();} catch(err) {}");
                }
            });
        }
        catch (Exception ex)
        {

        }

    }
    public void LoadLoadingcreen()
    {
        try {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("file:///android_asset/load_android.html");
                }
            });
        }
        catch (Exception ex)
        {

        }

    }
    public void doLookUpRegRtn(final String pRegNo, final String pResult, final String forceRefresh)
    {
        try {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mWebView.loadUrl("javascript:doLookUpRegRtn('" + pRegNo + "','" + pResult + "','" + forceRefresh + "');");
                    }
                    catch (Exception ex)
                    {
                        mWebView.loadUrl("javascript:doLookUpRegRtn('" + pRegNo + "','error:','" + forceRefresh + "');");
                    }
                }
            });
        }
        catch (Exception ex) {
            showError("Sorry, we had a problem looking up your car");
        }


    }
    public void CallUrlRtn(final String pResult, final String pLabel, final String pForceRefresh, final String pKey, final String pReturnFunc)
    {
        if (((appGlobal) mAct.getApplication()).getHtmlRunningStatus() == true) {
            mWebView2.post(new Runnable() {
                @Override
                public void run() {
                    mWebView2.loadUrl("about:blank");
                }
            });

            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:CallUrlRtn2('" + pResult + "','" + pLabel + "','" + pForceRefresh + "','" + pKey + "','" + pReturnFunc + "');");
                }
            });

        }
        ((appGlobal) mAct.getApplication()).setHtmlRunningStatus(false);
    }
    @JavascriptInterface
    public void StopAds() {
        try {
            mAct.StopAds();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    @JavascriptInterface
    public void StartAds() {
        try {
            mAct.StartAds();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    @JavascriptInterface
    public void StartBrainTree(final String pToken, final String pAmt, final String pProduct, final String pSku, final String pOriginalTask) {
        mAct.debugLog.d("INFO: ANDROID:", "StartBrainTree: sProduct: " + pProduct + " sFunctionToCall: " + pOriginalTask );
        if (mAct.mustardPurchases != null) {

            mAct.mustardPurchases.PaymentMode = "normal";
            mAct.mustardPurchases.ProductType = "product";
            mAct.mustardPurchases.BuyWithBraintree(pToken,pAmt,pProduct,pOriginalTask, new MustardPurchases.onBraintreeFinishedListener() {
                public void onFinished(String Status, String nonce, String extraData, String err) {
                    try {

                        mAct.debugLog.d("INFO: ANDROID:", "BuyWithBraintree: Status: " + Status + " pProduct:" + pProduct + " nonce:" + nonce + " sFunctionToCall: " + pOriginalTask + " error:" + err);

                        final String fStatus = Status;
                        final String fNonce = nonce;
                        final String fErr = err;


                        Handler mainHandler = new Handler(mCont.getMainLooper());
                        mainHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                mAct.htmlAPI.BraintreeRtn(fStatus,fNonce,fErr,pProduct,pSku,pOriginalTask);
                            }
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        mAct.htmlAPI.showError("We had an error trying to take payment");
                    }

                }

            });
        } else {
            mAct.debugLog.e("ERROR: ANDROID","APP: MainActivity doBilling payment not setup");
            mAct.htmlAPI.showError("Payment is not setup");
        }
    }
    @JavascriptInterface
    public void ShareFB(final String URL, final String title) {
        try {
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(URL))
                    .build();
            ShareDialog shareDialog = new ShareDialog(this.mAct);
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    @JavascriptInterface
    public void ClearHTML5() {
        try {
            File cacheDir = mCont.getCacheDir();

            File[] files = cacheDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if ((file.getName().endsWith(".js")) || (file.getName().endsWith(".html")) || (file.getName().endsWith(".css")))
                        file.delete();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }



    @JavascriptInterface
    public void CallURL(final String PayLoad, String pURL, final String pLabel, final String forceRefresh, final String pKey, final String pReturnFunc) {

        try {

            final GovAPI3 mGovtest = new GovAPI3(mAct,mCont, mWebView2, this);

            mWebView.post(new Runnable() {
                @Override
                public void run() {

                    String[] sparams = {pKey, forceRefresh, pReturnFunc};
                    ((appGlobal) mAct.getApplication()).setHtmlRunningStatus(true);
                    mGovtest.RunIt(sparams);
                }
            });


        }
        catch (Exception ex)
        {
            CallUrlRtn("error:" + ex.getMessage(), pLabel, forceRefresh, pKey, pReturnFunc);
        }

    }

    @JavascriptInterface
    public void CallURL_old(final String PayLoad, String pURL, final String pLabel, final String forceRefresh, final String pKey) {

        try {

            if (pURL.equals(""))
            {
                if (pLabel.equals("Gov1"))
                {
                    pURL = "https://vehicleenquiry.service.gov.uk/ConfirmVehicle";
                } else if (pLabel.equals("Gov2")){
                    pURL = "https://vehicleenquiry.service.gov.uk/ViewVehicle";
                }
            }

            CallURL callUrl = new CallURL(mAct,mCont);
            String[] sparams = {PayLoad,pURL, pLabel,forceRefresh};
            callUrl.RunIt(sparams, new  CallURL.onCallURLAPIReturnListener() {
                public void onFinished(String result) {
                    CallUrlRtn(result, pLabel, forceRefresh, pKey,"");
                }
            });
        }
        catch (Exception ex)
        {
            CallUrlRtn("error:" + ex.getMessage(), pLabel, forceRefresh, pKey,"");
        }

    }

    @JavascriptInterface
    public void LookUpReg(final String pRegNo, final String forceRefresh)
    {

        try {
            GovAPI govApi = new GovAPI(mAct,mCont);
            String[] sparams = {pRegNo};
            govApi.RunIt(sparams, new GovAPI.onGovAPIReturnListener() {
                public void onFinished(String result) {
                    doLookUpRegRtn(pRegNo, result,forceRefresh);
                }
            });
        }
        catch (Exception ex)
        {
            doLookUpRegRtn(pRegNo,"failed",forceRefresh);
        }

    }

    @JavascriptInterface
    public void restartload() {
        //mAct.clearCacheFolder(mCont.getCacheDir());
        ClearHTML5();
        Intent intent = new Intent(mAct, MainActivity.class);
        mAct.startActivity(intent);
        mAct.finish();
    }

    @JavascriptInterface
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mAct.getSystemService( mAct.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @JavascriptInterface
    public void passFrontEndReady(String sAppReady, String sAppVersion, String sUserID, String sAuthToken, String sDeviceID) {
        passFrontEndReady(sAppReady, sAppVersion, sUserID, sAuthToken, sDeviceID, "");
    }


    @JavascriptInterface
    public void passFrontEndReady(String sAppReady, String sAppVersion, String sUserID, String sAuthToken, String sDeviceID, String sFrontEndCanRun) {

        ((appGlobal) mAct.getApplication()).setFrontEndDone(true);

        try {
            mAct.htmlAPI.setAndroidSubVersionRtn();

        }
        catch (Exception ex)
        {
            mAct.debugLog.e("ERROR: ANDROID:"," passFrontEndReady setAndroidSubVersionRtn:" + ex.getMessage());
        }

        try {
            String referrer = ((appGlobal) mAct.getApplication()).getAppReferrer();
            String sEncoded = URLEncoder.encode(referrer, "UTF-8");
            mAct.htmlAPI.sendReferrer(sEncoded);
        }
        catch (Exception ex)
        {
            mAct.debugLog.e("ERROR: ANDROID:"," passFrontEndReady onPostExecute getAppReferrer:" + ex.getMessage());
        }


        ((appGlobal) mAct.getApplication()).setHTMl5Version(sAppVersion);
        if (sUserID != "0") {
            ((appGlobal) mAct.getApplication()).setAppUserID(sUserID);

        }
        if (sFrontEndCanRun != "yes") {
            ((appGlobal) mAct.getApplication()).setFrontEndCanRun(true);
        } else {
            ((appGlobal) mAct.getApplication()).setFrontEndCanRun(true);
        }
        if (sAuthToken != null && !sAuthToken.isEmpty())
        {
            ((appGlobal) mAct.getApplication()).setAuthToken(sAuthToken);
        }
        if (sDeviceID != null && !sDeviceID.isEmpty())
        {
            ((appGlobal) mAct.getApplication()).setAppDeviceId(sDeviceID);
        }

        ((appGlobal) mAct.getApplication()).setFrontEndReady(sAppReady);
        int duration = Toast.LENGTH_SHORT;
        mAct.debugLog.d("INFO: ANDROID:", "passFrontEndReady: sUserID: " + sUserID + " sAuthToken: " + sAuthToken );
        mAct.debugLog.d("INFO: ANDROID:", "passFrontEndReady: sDeviceID: " + sDeviceID);

        WebAPI webApi = new WebAPI(mCont);
        webApi.NotifyServerofopen(new WebAPI.onWebAPINotifyReturnListener() {
            public void onFinished(String result) {
                PostCheckRtn(result,"firstload");
            }
        });

    }

    private void PostCheckRtn(String result, String mode) {
        // NOTE USED IN BOTH startup AND resetuser
        if (result.equals("maintenance"))
        {
             mAct.htmlAPI.LoadMaintenanceScreen();
        }
        else if (result != "FAILED" && result != "TIMEOUT") {
            String sShouldDebug = "no";
            String sUserID = "";
            String sReLoad = "no";
            String sTimings = "5,15";
            String sAuthToken = "";
            String sAppMode = "";
            String sAppType = "";
            String sJWT = "";
            try {
                JSONObject jRes = new JSONObject(result);
                sUserID = jRes.get("UserId").toString();
                sAuthToken = jRes.get("AuthToken").toString();
                sAppMode = jRes.get("AppMode").toString();
                sAppType = jRes.get("AppType").toString();
                sShouldDebug = jRes.get("DebugRequired").toString();
                if (sShouldDebug.equalsIgnoreCase("yes"))
                    ((appGlobal) mAct.getApplication()).setShouldDebug(true);
                sReLoad = jRes.get("Reload").toString();
                sTimings = jRes.get("timings").toString();
                sJWT = jRes.get("JWT").toString();
                try {
                    ((appGlobal) mAct.getApplication()).setGovStr(jRes.get("gStr").toString());
                }
                catch (Exception ex) {}

                mAct.debugLog.d("INFO: ANDROID:", "NotifyServerofopen: sUserID: " + sUserID + " sAuthToken: " + sAuthToken );

            } catch (JSONException e) {

                e.printStackTrace();
            }
            try {
                ((appGlobal) mAct.getApplication()).setAppUserID(sUserID);
            }
            catch (Exception ex) {
                mAct.debugLog.d("ERROR: ANDROID:", "NotifyServerofopen: getApplication. setAppUserID: " + sUserID);
            }
            try {
                ((appGlobal) mAct.getApplication()).setAuthToken(sAuthToken);
            }
            catch (Exception ex) {
                mAct.debugLog.d("ERROR: ANDROID:", "NotifyServerofopen: getApplication. setAuthToken: " + sAuthToken);
            }
            try {
                ((appGlobal) mAct.getApplication()).setAppMode(sAppMode);
            }
            catch (Exception ex) {
                mAct.debugLog.d("ERROR: ANDROID:", "NotifyServerofopen: getApplication. sAppMode: " + sAppMode);
            }
            try {
                ((appGlobal) mAct.getApplication()).setAppType(sAppType);
            }
            catch (Exception ex) {
                mAct.debugLog.d("ERROR: ANDROID:", "NotifyServerofopen: getApplication. sAppType: " + sAppType);
            }

            if (sReLoad.toLowerCase().contains("yes")) {
                mAct.mWebView.clearCache(true);
                ((appGlobal) mAct.getApplication()).setFrontEndReady("");
                //mAct.clearCacheFolder(mCon.getCacheDir());
                UpdatingScreen();
                mAct.htmlAPI.LoadMainScreen(true);
            }
            else {
                mAct.htmlAPI.SetUserID(sUserID);
                mAct.htmlAPI.JWTrtn(sJWT);
                if (mode == "resetuser") {
                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl("javascript:ShowHomeDelayed();");
                        }
                    });
                }
                if (!mAct.NotificationPayLoad.equals("")) {
                    mAct.htmlAPI.NotificationDataRtn(mAct.NotificationPayLoad);
                }
                try {
                    Uri data = mAct.getIntent().getData();
                    String scheme = data.getScheme(); // "http"
                    if (scheme.equals("https")) {
                        String lquery = data.getQuery();
                        if (!lquery.isEmpty()) {
                            mAct.htmlAPI.NotificationDataRtn(lquery.replace("data=",""));
                        }
                         mAct.getIntent().setData(null);
                    }
                 }
                 catch (Exception ex)
                 {
                 }
            }
        } else if (result == "TIMEOUT") {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    if (!((appGlobal) mAct.getApplication()).getFrontEndCanRun())
                        mWebView.loadUrl("file:///android_asset/reloadandroid.html");
                }
            });
        }
        else {
            if (!((appGlobal) mAct.getApplication()).getFrontEndCanRun())
                mAct.htmlAPI.showError("Failed to contact our servers");

        }
    }
    @JavascriptInterface
    public void resetAppUserId(String sUserID, String sAuthToken, String sDeviceID) {

        if (sUserID != "0") {
            ((appGlobal) mAct.getApplication()).setAppUserID(sUserID);
        }
        if (sAuthToken != null && !sAuthToken.isEmpty())
        {
            ((appGlobal) mAct.getApplication()).setAuthToken(sAuthToken);
        }
        if (sDeviceID != null && !sDeviceID.isEmpty())
        {
            ((appGlobal) mAct.getApplication()).setAppDeviceId(sDeviceID);
        }
        mAct.htmlAPI.SetUserID(sUserID);
        mAct.htmlAPI.PostLoginRtn();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        mAct.mCookieManager.flush();
                        mAct.debugLog.d("INFO: ANDROID:", "resetAppUserId Cookies sync");
                    }
                },
                1000);
    }
    @JavascriptInterface
    public void doWebPage(String sUrl, String sType, String sPayLoad) {

        Intent myIntent = new Intent(mAct, webViewActivity.class);
        myIntent.putExtra("Url", sUrl);
        myIntent.putExtra("PageType", sType);
        myIntent.putExtra("PayLoad", sPayLoad);
        mAct.startActivityForResult(myIntent,mAct.RC_SAVEURL);

    }
    @JavascriptInterface
    public void askForNotification() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mCont);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        if (!areNotificationsEnabled) {
            Intent myIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
                myIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                myIntent.putExtra("android.provider.extra.APP_PACKAGE", mCont.getPackageName());
            }else if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                myIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                myIntent.putExtra("app_package", mCont.getPackageName());
                myIntent.putExtra("app_uid", mCont.getApplicationInfo().uid);
            }else {
                myIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                myIntent.addCategory(Intent.CATEGORY_DEFAULT);
                myIntent.setData(Uri.parse("package:" + mCont.getPackageName()));
            }
            mAct.startActivityForResult(myIntent, mAct.RC_SETTINGS);
        } else {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:askForNotificationRtn('ok');");
                }
            });
        }
    }
    @JavascriptInterface
    public void replaceURL(final String pUrl) {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(pUrl);
            }
        });
    }
    @JavascriptInterface
    public void checkNotification() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mCont);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        String lRes = "no";
        if (areNotificationsEnabled) lRes = "yes";
        final String lResFinal = lRes;
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:isCheckNotificationsRtn('" + lResFinal + "');");
            }
        });
    }
    @JavascriptInterface
    public void checkLocationServices() {

        String lRes = "no";
        if (mAct.LocationIsOn) lRes = "yes";
        final String lResFinal = lRes;
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:checkLocationServicesRtn('" + lResFinal + "');");
            }
        });

    }
    @JavascriptInterface
    public void TurnOnLocationServices() {
        if (mAct.LocationIsOn) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:try {TurnOnLocationServicesRtn('done')} catch(err) {};");
                }
            });
        } else {
            mAct.AskForLocation();
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:try {TurnOnLocationServicesRtn('done');} catch(err) {}");
                }
            });
        }

    }
    @JavascriptInterface
    public void ClipBoard(final String pLabel, final String pText) {

            mWebView.post(new Runnable() {
                @Override
                public void run() {
                //    mAct.debugLog.d("INFO: ANDROID:", "ClipBoard: pLabel: " + pLabel + " pText:" + pText );
                    ClipboardManager clipboard = (ClipboardManager) mAct.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(pLabel, pText);
                    clipboard.setPrimaryClip(clip);

                    Toast toast = Toast.makeText(mAct.getApplicationContext(), pLabel + " copied to clipboard", Toast.LENGTH_LONG);
                    View view = toast.getView();
                    view.setBackgroundResource(android.R.drawable.toast_frame);
                    toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    try {
                        mWebView.loadUrl("javascript:ClipBoardRtn('" + pLabel + "');");
                    }
                    catch (Exception ex) {
                        mAct.debugLog.d("ERROR: ANDROID:", "ClipBoard: error: " + ex.getMessage() );
                    }
                }
            });

    }

    @JavascriptInterface
    public String runloc() {

/*
       if ( mAct.Latitude=="0") {
            if (ContextCompat.checkSelfPermission(mWebView.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {


*/
/*                mAct.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mAct.mGoogleApiClient);
                if (mAct.mLastLocation != null) {
                    mAct.Latitude = String.valueOf(mAct.mLastLocation.getLatitude());
                    mAct.Longitude = String.valueOf(mAct.mLastLocation.getLongitude());
                }*//*

            }
        }
*/

        return(mAct.Latitude+","+mAct.Longitude);

    }
    @JavascriptInterface
    public void SetAppType(final String sAppType) {
        try {
            ((appGlobal) mAct.getApplication()).setAppType(sAppType);

//            if (sAppType.equals("Premium")) {
//                Handler mainHandler = new Handler(mCont.getMainLooper());
//                mainHandler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        mAct.mAdContainer.setVisibility(View.INVISIBLE);
//                        if (mAct.adView != null)
//                            mAct.adView.destroy();
//                    }
//                });
//            }
        }
        catch (Exception ex) {

            mAct.debugLog.d("ERROR: ANDROID:", "SetAppType: error: " + ex.getMessage() );
        }
    }

    @JavascriptInterface
    public void doRewardVideo(final String sProduct, final String sFunctionToCall, final String sParams) {
//        mAct.debugLog.d("INFO: ANDROID:", "doRewardVideo: sProduct: " + sProduct + " sFunctionToCall: " + sFunctionToCall );
//        mAct.mustardRewards.showRewardedVideoAd();

    }

    @JavascriptInterface
    public void doBilling(final String sProduct, final String sSku, final String sFunctionToCall, final String sParams) {
        mAct.debugLog.d("INFO: ANDROID:", "doBilling: sProduct: " + sProduct + " sFunctionToCall: " + sFunctionToCall );
        if (mAct.mustardPurchases != null) {
            if (!mAct.mustardPurchases.mIsSetUp) {
                Handler mainHandler = new Handler(mCont.getMainLooper());
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mAct.htmlAPI.doBillingRtn("NotSupported", sProduct, sFunctionToCall, sParams);
                    }
                });
                return;

            }
            mAct.mustardPurchases.PaymentMode = "normal";
            mAct.mustardPurchases.ProductType = "product";
            mAct.mustardPurchases.Buy(sSku, sFunctionToCall, sProduct, new MustardPurchases.onPurchaseCompleteListener() {
                public void onFinished(String result) {
                    try {

                        String retVal = "FAILED";
                        retVal = result;
                        mAct.debugLog.d("INFO: ANDROID:", "doBilling: result: " + result + " sProduct:" + sProduct + " sSku:" + sSku + " sFunctionToCall: " + sFunctionToCall );
                        mAct.debugLog.d("INFO: ANDROID:","onPaymentWebAPIReturn result:" + result);

                        final String fRetVal = retVal;
                        Handler mainHandler = new Handler(mCont.getMainLooper());
                        mainHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                mAct.htmlAPI.doBillingRtn(fRetVal, sProduct, sFunctionToCall, sParams);
                            }
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        mAct.htmlAPI.showError("We had an error trying to take payment");
                    }

                }

            });
        } else {
            mAct.debugLog.e("ERROR: ANDROID","APP: MainActivity doBilling payment not setup");
            mAct.htmlAPI.showError("Payment is not setup");
        }

    }
    @JavascriptInterface
    public void dorestore(String sfingerprint) {
        //if (sfingerprint == ((appGlobal) this.activity.getApplication()).getappFingerprint())
        //{
        mAct.debugLog.e("ERROR: ANDOIRD:","Starting restore in app");
        mAct.ShouldIBill = "yes";

        // TODO: 22/09/2017
        //mHelper.mPaymentMode = "restore";
        // mHelper.queryInventoryAsync(mGotInventoryListener);
        //}
    }
    @JavascriptInterface
    public void clearCache()
    {
        mAct.clearCacheFolder(mCont.getCacheDir());
        Handler mainHandler = new Handler(mCont.getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                mAct.htmlAPI.LoadMainScreen(true);
            }
        });

    }

    @JavascriptInterface
    public void facebookshare(String furl)
    {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(furl))
                .build();
    }
    @JavascriptInterface
    public void facebookLogin(String htmlMode)
    {
        mAct.FaceBookHTMLMode = htmlMode;
        LoginManager.getInstance().logInWithReadPermissions(mAct, Arrays.asList("public_profile", "email"));
        //AccessToken token = AccessToken.getCurrentAccessToken();
        //updateWithToken(token);
    }
    @JavascriptInterface
    public void facebookLogout()
    {
        LoginManager.getInstance().logOut();
        AccessToken token = AccessToken.getCurrentAccessToken();
        mAct.updateWithToken(token);
    }
    @JavascriptInterface
    public void facebookRefer()
    {
        String appLinkUrl, previewImageUrl;
        final String tUserID = ((appGlobal) mAct.getApplication()).getAppUserID();
        if (BuildConfig.DEBUG) {
           // appLinkUrl = mAct.getResources().getString(R.string.mainDomain_debug) + "fb-refer.aspx?referrer="+tUserID;
            appLinkUrl=mAct.getResources().getString(R.string.mainDomain_debug)+"fb-refer.aspx?referrer="+tUserID;
            previewImageUrl = mAct.getResources().getString(R.string.mainDomain_debug) + "images/fb-refer-image.jpg";
        } else {
            appLinkUrl=mAct.getResources().getString(R.string.mainDomain)+"fb-refer.aspx?referrer="+tUserID;
            previewImageUrl = mAct.getResources().getString(R.string.mainDomain) + "images/fb-refer-image.jpg";

        }
        mAct.mCallbackManager = CallbackManager.Factory.create();
        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
                    .setPromotionDetails("StriveInvite","fbinvite")
                    .setPreviewImageUrl(previewImageUrl)
                    .build();
            AppInviteDialog appInviteDialog = new AppInviteDialog(mAct);
            appInviteDialog.registerCallback(mAct.mCallbackManager, new FacebookCallback<AppInviteDialog.Result>()
            {
                @Override
                public void onSuccess(AppInviteDialog.Result result)
                {
                    mAct.htmlAPI.fbInviteRtn("OK");
                }

                @Override
                public void onCancel()
                {
                }

                @Override
                public void onError(FacebookException e)
                {
                    mAct.debugLog.e("ERROR: ANDROID:","facebook referrer error:" + e.getMessage());
                }
            });
            AppInviteDialog.show(mAct, content);
        }
    }
    @JavascriptInterface
    public void googleLogin(String htmlMode)
    {
        mAct.googleSignIn();
    }
    @JavascriptInterface
    public void googleLogout()
    {
        mAct.googleSignOut();

    }

    @JavascriptInterface

    public void clearAllCookies() {
        mCookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                Log.d("Activity", "html clearCookies completed with: " + value + ", now flushing:");
                mCookieManager.flush();
            }
        });
    }

    @JavascriptInterface
    public void resetUser() {
        ((appGlobal) this.mAct.getApplication()).setFrontEndReady("OK");
        ((appGlobal) this.mAct.getApplication()).setAuthToken("none");
        ((appGlobal) this.mAct.getApplication()).setAppUserID("");
        String android_id = Settings.Secure.getString(this.mAct.getContentResolver(), Settings.Secure.ANDROID_ID);
        ((appGlobal) this.mAct.getApplication()).setAppDeviceId(android_id);
        mCookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                Log.d("Activity", "html clearCookies completed with: " + value + ", now flushing:");
                mCookieManager.flush();
                WebAPI webApi = new WebAPI(mCont);
                webApi.NotifyServerofopen(new WebAPI.onWebAPINotifyReturnListener() {
                    public void onFinished(String result) {
                        PostCheckRtn(result,"resetuser");
                    }
//                WaitForFrontEnd waitForFrontEnd = new WaitForFrontEnd(mAct,mCont);
//                waitForFrontEnd.RunIt(new WaitForFrontEnd.onReturnListener() {
//                    @Override
//                    public void onFinished(String result) {
//                        if (result == "OK")
//                        {
//                            try {
//                                mWebView.loadUrl("javascript:ShowHomeDelayed();");
//                            }
//                            catch (Exception ex)
//                            {
//                                ex.printStackTrace();
//                            }
//
//                            //LoadMainScreen(false);
//
//                        } else {
//                            //   mAct.ShowDialogMessage("Failed to reset your account");
//                            showError("Sorry, something went wrong");
//                        }
//                    }
//                } );
                });
            }
        });
    }

}

