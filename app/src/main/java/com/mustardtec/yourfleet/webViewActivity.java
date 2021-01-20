package com.mustardtec.yourfleet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

//import com.mustardtec.yourfleet.util.WaitForFrontEnd;

import androidx.appcompat.app.AppCompatActivity;

public class webViewActivity extends AppCompatActivity {

    private WebView mWebView = null;
    public String PageType = "";
    public String Url = "";
    public String PayLoad = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Context context = getApplicationContext();

        Intent intent = getIntent();
        Url = intent.getStringExtra("Url");
        PageType = intent.getStringExtra("PageType");
        PayLoad = intent.getStringExtra("PayLoad");

        Button saveBut = (Button) findViewById(R.id.SaveForLater);
        if (PageType.equals("NoSave"))
            saveBut.setVisibility(View.GONE);

        this.mWebView = (WebView) findViewById(R.id.externalWebView);
      //  this.mWebView.addJavascriptInterface(new HtmlAPI(this,this.mWebView,context), "androidWebOnline");
        this.mWebView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = this.mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setBackgroundColor(Color.parseColor("#1e2229"));
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setTextZoom(100);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setWebViewClient(new WebViewClient() {
            boolean timeout;
            boolean webViewError;
            boolean mainLoad;
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            public void onPageFinished(WebView view, String url) {

            }

            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){


            }
            public void onReceivedHttpError(WebView view,
                                            WebResourceRequest request, WebResourceResponse errorResponse) {

                super.onReceivedHttpError(view, request, errorResponse);

            }

        });

        mWebView.loadUrl(Url);
    }
    public void closeView(View view) {

        Intent intent = new Intent();
        intent.putExtra("Url", "");
        intent.putExtra("PageType", PageType);
        setResult(this.RESULT_OK, intent);
        this.finish();
    }
    public void saveAction(View view) {
        Intent intent = new Intent();
        intent.putExtra("Url", mWebView.getUrl());
        intent.putExtra("PageType", PageType);
        setResult(this.RESULT_OK, intent);
        finish();
    }

    public void LoadTheUrl(String pUrl)
    {
        try {
                mWebView.loadUrl(pUrl);

        }
        catch (Exception ex)
        {

        }

    }
}
