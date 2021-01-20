package com.mustardtec.yourfleet;

import com.braintreepayments.api.dropin.DropInActivity;
import com.facebook.internal.CallbackManagerImpl;
//import com.google.android.gms.ads.MobileAds;

import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;

import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;


import java.io.File;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import com.facebook.Profile;
//import com.facebook.ads.*;

import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.applinks.AppLinkData;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.mustardtec.yourfleet.util.MustardPurchases;
//import com.mustardtec.yourfleet.util.MustardRewardVideo;

import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;

import bolts.AppLinks;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, OnConnectionFailedListener, KeyEvent.Callback {
    protected static boolean isVisible = false;

    public WebView mWebView = null;
    public android.webkit.CookieManager mCookieManager = null;
    public MustardPurchases mustardPurchases = null;
//    public MustardRewardVideo mustardRewards = null;
    static final String SKU_Product = "yourfleet.five.credits";
    //static final String SKU_Product = "android.test.refunded";

    private FirebaseAuth mAuth;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    public static final int RC_SAVEURL = 2340;
    public static final int RC_SETTINGS = 3030;
    public static final int RC_DROP_IN_REQUEST = 3340;

    public static final int BT_RESULT_CANCELED = 0;
    public static final int BT_RESULT_FIRST_USER = 1;
    public static final int BT_RESULT_OK = -1;

    public FusedLocationProviderClient mFusedLocationClient;
    public LocationCallback mLocationCallback;
    public LocationRequest mLocationRequest;
    public boolean LocationIsOn = false;
    String android_id = "";
    String referrer = "";
    public String ShouldIBill = "no";
    public int adFirst = 5;
    public int adSub = 15;
    public String Latitude = "0";
    public String Longitude = "0";
    public boolean showAds = false;
  //  ProgressDialog dialog;
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    public CallbackManager mCallbackManager;
    public static String FaceBookHTMLMode = "";
    int MY_PERMISSIONS_FINE_ACCESS;
    public String NotificationPayLoad = "";
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 0;
    public HtmlAPI htmlAPI = null;
    public DebugLog debugLog = null;
    Boolean mFinishedLoading = false;
   // public AdView adView;
    public boolean PremiumUser = false;
    public boolean IsPhone = false;
    public LinearLayout mAdContainer;

    public final String AppVersionNo = "7";
    public final String AppSubVersionNo = "14";
    // braintree




    private static final String KEY_NONCE = "nonce";

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (ContextCompat.checkSelfPermission(mWebView.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    LocationIsOn = true;
                                    Latitude = String.valueOf(location.getLatitude());
                                    Longitude = String.valueOf(location.getLongitude());
                                    mWebView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mWebView.loadUrl("javascript:try {TurnOnLocationServicesRtn('done');} catch(err) {}");
                                        }
                                    });
                                } else {
                                    LocationIsOn = false;
                                    mWebView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mWebView.loadUrl("javascript:try {TurnOnLocationServicesRtn('done');} catch(err) {}");
                                        }
                                    });
                                }

                                startLocationUpdates();
                            }
                        });
            }
            catch (Exception ex) {
                debugLog.e("ERROR: ANDROID:","MainActivity: onRequestPermissionsResult getLastLocation err:" + ex.getMessage());
            }
        } else {
            LocationIsOn = false;
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDestroy() {
//        if (mAdView != null) {
//            mAdView.destroy();
//        }
        if (mustardPurchases != null)
        {
            mustardPurchases.Dispose();
        }
//        if (adView != null) {
//            adView.destroy();
//        }

        super.onDestroy();
    }

    public void updateWithToken(AccessToken currentAccessToken) {
        try {
            if (currentAccessToken != null) {
                Handler mainHandler = new Handler(mWebView.getContext().getMainLooper());
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        Profile profile = Profile.getCurrentProfile();
                       //PwStorage hashing = new PwStorage();
                       // String l_s_hash = hashing.main(profile.getName() + "###" + profile.getId());
                        htmlAPI.fbLogoutRtn(profile.getId(),profile.getName());
                    }
                });



            } else {
                String abc="a";
            }
        } catch(Exception e) {
            debugLog.d("Error", String.format("Failed to clean the cache, error %s", e.getMessage()));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void AskForLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_FINE_ACCESS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

        }
    }

    public static int clearCacheFolder(final File dir) {
        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    // if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                    if (child.delete()) {
                        deletedFiles++;
                    }
                    //}
                }
            }
            catch(Exception e) {
                Log.e("clear cache", String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        //setVisible(false);
        //isVisible = false;
        if (((appGlobal) this.getApplicationContext()).getFrontEndDone()) {
            this.htmlAPI.PauseFrontEnd();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        setVisible(true);
        startLocationUpdates();
        isVisible = true;
        if (((appGlobal) this.getApplicationContext()).getFrontEndDone()) {
            this.htmlAPI.AppCameintoForeGround();
            this.htmlAPI.setAndroidSubVersionRtn();
            try {
                Uri data = getIntent().getData();
                String scheme = data.getScheme(); // "http"
                if (scheme.equals("https")) {
                    String lquery = data.getQuery();
                    if (!lquery.isEmpty()) {
                        htmlAPI.NotificationDataRtn(lquery.replace("data=", ""));
                    }
                }
            } catch (Exception ex) {

            }
        } else {

        }


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        ((appGlobal) this.getApplication()).setFrontEndDone(false);

        final Context context = getApplicationContext();

        ((appGlobal) this.getApplication()).setAppVersion(AppVersionNo);
        ((appGlobal) this.getApplication()).setAppSubVersion(AppSubVersionNo);
        if (!isTablet(context))
            IsPhone = true;


        NotificationPayLoad = "";
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                if (key.equals("NotifyData")) {
                    NotificationPayLoad = value;
                }
                if (key.equals("ReferrerData"))
                    ((appGlobal) getApplication()).setAppReferrer(value);
             //   Log.d("MainActivity", "BundleKey: " + key + " Value: " + value);
            }
        }

        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.mustardtec.yourfleet",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        }




        String tempApp = ((appGlobal) this.getApplication()).getFrontEndReady();
        if (tempApp == "" || tempApp == null) {
            // only do this on fresh load otherwise you might overwrite a android_id in play
            android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            ((appGlobal) this.getApplication()).setAppDeviceId(android_id);
        }

        RelativeLayout popwindow=new RelativeLayout(this);
        final WebView mWebView2 = new WebView(this);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mWebView2.setLayoutParams(params);
        mWebView2.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings2 = mWebView2.getSettings();
        webSettings2.setJavaScriptEnabled(true);
        webSettings2.setAllowFileAccessFromFileURLs(true);
        webSettings2.setAllowUniversalAccessFromFileURLs(true);
        webSettings2.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings2.setAllowFileAccess(true);
        webSettings2.setAppCacheEnabled(true);
        webSettings2.setJavaScriptEnabled(true);
        webSettings2.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings2.setDomStorageEnabled(true);
        webSettings2.setTextZoom(100);
        webSettings2.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings2.setSupportMultipleWindows(true);


        popwindow.addView(mWebView2);
        this.addContentView(popwindow,params);

        debugLog = new DebugLog(context);
        this.mWebView = (WebView) findViewById(R.id.webView);


        WebView wView = (WebView) findViewById(R.id.webViewLoading);
        wView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettingsL = wView.getSettings();
        wView.setBackgroundColor(Color.parseColor("#1e2229"));
        webSettingsL.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettingsL.setAppCacheEnabled(true);
        webSettingsL.setTextZoom(100);
        webSettingsL.setJavaScriptEnabled(true);
        webSettingsL.setCacheMode(WebSettings.LOAD_DEFAULT);
        wView.loadUrl("file:///android_asset/androidloading.html");
        //   setContentView(wView);


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
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setTextZoom(100);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setSupportMultipleWindows(true);

       // webSettings.setSafeBrowsingEnabled(false);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // load online by default

        this.mCookieManager = android.webkit.CookieManager.getInstance();
        this.mCookieManager.setAcceptThirdPartyCookies(this.mWebView, true);
        this.mCookieManager.removeSessionCookies(null);
        htmlAPI = new HtmlAPI(MainActivity.this,mWebView, mWebView2, context, mCookieManager);
        this.mWebView.addJavascriptInterface(htmlAPI, "androidOnline");

        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.setWebContentsDebuggingEnabled(true);
            }
        }

        debugLog.mHtmlAPI = htmlAPI;

        try {
            htmlAPI.LoadMainScreen(false);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));

        }*/

        //FacebookSdk.sdkInitialize(this);
        try {
            Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
            //FacebookSdk.setApplicationId("201072610344961");
            //FacebookSdk.sdkInitialize(this);
            //FacebookSdk.sdkInitialize(this);
            //new sErrlog().execute("about to pass  you targetURL");
            if (targetUrl != null) {
                Bundle applinkData = AppLinks.getAppLinkData(getIntent());
                referrer = applinkData.getString("referral");
                if (referrer == null) {
                    Uri data = getIntent().getData();
                    referrer = data.getQueryParameter("referral");
                }
                if (referrer != "") {
                    ((appGlobal) getApplication()).setAppReferrer(referrer);
                }
                debugLog.d("Activity", "App Link Target URL: " + targetUrl.toString());
            }
        }
        catch (Exception ex) {
            debugLog.d("ERROR: Activity", "getTargetUrlFromInboundIntent: " + ex.getMessage());
        }

        //deferred
        try
        {
            AppLinkData.fetchDeferredAppLinkData(this,
                    new AppLinkData.CompletionHandler() {
                        @Override
                        public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                            if (appLinkData != null) {
                                Bundle bundle = appLinkData.getArgumentBundle();
                                //new sErrlog().execute("in deferr deeplink bundle:"+ bundle.toString());
                                if (bundle != null)
                                {
                                    String referredurl = bundle.getString("target_url");
                                    String l_s_temp = "";
                                    if (referredurl.contains("referral")) {
                                        int startPos = referredurl.indexOf("referral") + 9;
                                        l_s_temp = referredurl.substring(startPos,referredurl.length());
                                    }

                                    debugLog.d("INFO: ANDROID:", " referrer:" + referrer);
                                    if (l_s_temp != "") {
                                        //new sErrlog().execute("in deferr deeplink got referrer:"+ l_s_temp);
                                        referrer = l_s_temp;
                                        WebAPI webApi = new WebAPI(context);
                                        String UserId = ((appGlobal) context.getApplicationContext()).getAppUserID();
                                        debugLog.d("INFO: ANDROID:", " Referral USERID:" + UserId + " referrer:" + referrer);
                                        if (UserId != "") {
                                            // if we've got a userid it means the referral didn't get sent via notifyopen
                                            webApi.Controller = "app";
                                            webApi.Action = "UpdateReferral";
                                            String[] sparams = {l_s_temp, "", "", "", "", ""};
                                            webApi.RunIt(sparams, new WebAPI.onWebAPIReturnListener() {
                                                public void onFinished(String result) {

                                                }
                                            });
                                        }
                                    }
                                }

                            } else {
                                //new sErrlog().execute("in deffer deeplink bendle is null");
                            }
                        }
                    });
        }
        catch (Exception e)
        {
            debugLog.d("ERROR: Activity", "App Links: " + e.getMessage());
        }



        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    //private ProfileTracker mProfileTracker;
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
                        final String l_s_accesstoken = accessToken.getToken();
                        //Profile profile = Profile.getCurrentProfile();
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {

                                        try {
                                            Log.v("LoginActivity", response.toString());
                                            // Application code
                                            String email = object.getString("email");
                                            String name = object.getString("name");
                                            String id = object.getString("id");
                                            htmlAPI.fbLoginRtn(id, name, l_s_accesstoken, email);
                                        }
                                        catch (JSONException e) {
                                            htmlAPI.showError("we had a problem: " + e.getMessage());
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();

//                        try {
//                            final AccessToken accessToken = AccessToken.getCurrentAccessToken();
//                            final String l_s_accesstoken = accessToken.getToken();
//                            if (Profile.getCurrentProfile() == null) {
//                                mProfileTracker = new ProfileTracker() {
//                                    @Override
//                                    protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
//                                        // profile2 is the new profile
//                                        //Profile profile2 = Profile.getCurrentProfile();
//                                        PwStorage hashing = new PwStorage();
//                                        htmlAPI.fbLoginRtn(profile2.getId(), profile2.getName(), l_s_accesstoken, profile.);
//                                        //    htmlAPI.fbLoginRtn(hashing.main( profile2.getName() + "###" + profile2.getId() ),profile2.getId(),profile2.getName(),l_s_accesstoken,FaceBookHTMLMode);
//
//                                        mProfileTracker.stopTracking();
//                                    }
//                                };
//                                mProfileTracker.startTracking();
//                            } else {
//                                Profile profile = Profile.getCurrentProfile();
//                                PwStorage hashing = new PwStorage();
//                                htmlAPI.fbLoginRtn(profile.getId(), profile.getName(), l_s_accesstoken, "none");
//                                // htmlAPI.fbLoginRtn(hashing.main( profile.getName() + "###" + profile.getId() ),profile.getId(),profile.getName(),l_s_accesstoken,FaceBookHTMLMode);
//
//                            }
//                            debugLog.d("Success", "Login");
//                        }
//                        catch (Exception ex) {
//                            htmlAPI.showError("we had a problem: " + ex.getMessage());
//
//                        }

                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });



/*        dialog = new ProgressDialog(this.mWebView.getContext());
        dialog.setMessage("Loading, please wait");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.setIndeterminate(true);
        dialog.show();*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_FINE_ACCESS);
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }

        mAuth = FirebaseAuth.getInstance();


//        MobileAds.initialize(this, context.getResources().getString(R.string.adMobID));



        mustardPurchases = new MustardPurchases(MainActivity.this, context);

  //      mustardRewards = new MustardRewardVideo(this,context);

   //     mustardRewards.loadRewardedVideoAd();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        LocationIsOn = true;
                        Latitude = String.valueOf(location.getLatitude());
                        Longitude = String.valueOf(location.getLongitude());
                    } else {
                        LocationIsOn = false;
                    }
                }
            };
        };

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

/*
        mustardRewards.mRewardedVideoAd.loadAd(appId,
                new AdRequest.Builder().build());
*/




        if (ContextCompat.checkSelfPermission(mWebView.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LocationIsOn = true;
                                Latitude = String.valueOf(location.getLatitude());
                                Longitude = String.valueOf(location.getLongitude());
                            } else {
                                LocationIsOn = false;
                            }
                            startLocationUpdates();
                        }
                    });
        }


//        String appType = ((appGlobal) context.getApplicationContext()).getAppVersion();
//        if (appType != "Premium") {
//            if (IsPhone) {
//                adView = new AdView(this, "482272828815463_612607762448635", AdSize.BANNER_HEIGHT_50);
//            } else {
//                adView = new AdView(this, "482272828815463_612607762448635", AdSize.BANNER_HEIGHT_90);
//            }
//
//            // Find the Ad Container
//            mAdContainer = (LinearLayout) findViewById(R.id.banner_container);
//            TextView valueTV = new TextView(this);
//            valueTV.setText("Why ads?");
//            valueTV.setPadding(20,5,30,15);
//            valueTV.setGravity(Gravity.RIGHT);
//            valueTV.setBackgroundColor(Color.parseColor("#e8e8e8"));
//            valueTV.setTextColor(Color.parseColor("#21afdf"));
//            valueTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//            valueTV.setOnClickListener(mThisButtonListener);
//
////            Button btnTag = new Button(this);
////            btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
////            btnTag.setText("Button");
////
////            adContainer.addView(btnTag);
////           // btnTag.setId(1);
//
//
//            mAdContainer.addView(valueTV);
//            mAdContainer.setVisibility(View.INVISIBLE);
//            mAdContainer.setBackgroundColor(Color.parseColor("#ffffff"));
//
//            // Add the ad view to your activity layout
//            mAdContainer.addView(adView);
//
//            adView.setAdListener(new AdListener() {
//                @Override
//                public void onError(Ad ad, AdError adError) {
//                    // Ad error callback
//                    mAdContainer.setVisibility(View.INVISIBLE);
//                }
//
//                @Override
//                public void onAdLoaded(Ad ad) {
//                    // Ad loaded callback
//                    if (mAdContainer.getVisibility() != View.VISIBLE)
//                        mAdContainer.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAdClicked(Ad ad) {
//                    // Ad clicked callback
//                }
//
//                @Override
//                public void onLoggingImpression(Ad ad) {
//                    // Ad impression logged callback
//                }
//            });
//            AdSettings.addTestDevice("be1a9987-ab5f-4eb8-8dcb-b5ef0cb11945");
            // Request an ad
//            if ((!PremiumUser) && (IsPhone)) {
//
//
//
//                adView.loadAd();
//
//
//                LoadNewAdd();
//
//            }
//        }

    }
    @SuppressWarnings("deprecation")

    public void clearCookies()
    {
        if (BuildConfig.DEBUG) {
            this.debugLog.d("Activity", "html Using clearCookies userid =" + mCookieManager.getCookie(this.getResources().getString(R.string.mainDomain_debug) + "v" + ((appGlobal)this.getApplicationContext()).getAppVersion() + "/index2.html?platform=android"));
        } else {
            this.debugLog.d("Activity", "html Using clearCookies userid =" + mCookieManager.getCookie(this.getResources().getString(R.string.mainDomain_debug) + "v" + ((appGlobal)this.getApplicationContext()).getAppVersion() + "/index2.html?platform=android"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            this.debugLog.d("Activity", "Using html clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            mCookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    Log.d("Activity", "html clearCookies completed with: " + value + ", now flushing:");
                    mCookieManager.flush();
                }
            });


        } else
        {
            this.debugLog.d("Activity", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr= CookieSyncManager.createInstance(this.getApplicationContext());
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
    public void StartAds()
    {
//        if ((!PremiumUser) && (IsPhone)) {
//
//            //  adView.destroy();
//            if (isVisible)
//                adView.loadAd();
//            LoadNewAdd();
//        }
    }
    public void StopAds()
    {
//       adView.destroy();
    }
//    public void LoadNewAdd()
//    {
//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        try {
//                            if ((!PremiumUser) && (IsPhone)) {
//
//                                //  adView.destroy();
//                                if (isVisible)
//                                    adView.loadAd();
//                                LoadNewAdd();
//                            }
//                        }
//                        catch (Exception ex) {
//
//                        }
//
//                    }
//                },
//                30000);
//    }

    private View.OnClickListener mThisButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            htmlAPI.ShowPremium();
        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        else
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        try {
            mGoogleApiClient.connect();
        }
        catch (Exception ex)
        {
            debugLog.e("ERROR: ANDROID:","onStart mGoogleApiClient:" + ex.getMessage());
        }
        super.onStart();

        Context context = getApplicationContext();

        CookieManager cookieManager = CookieManager.getInstance();

        IntentFilter f=new IntentFilter(MyFirebaseMessagingService.ACTION_COMPLETE);

        try {
            LocalBroadcastManager.getInstance(context)
                    .registerReceiver(onEvent, f);
        }
        catch (Exception ex)
        {
            debugLog.e("ERROR: ANDROID:","onStart LocalBroadcastManager:" + ex.getMessage());
        }
        try {
            if ((mustardPurchases.mDisposed) || (mustardPurchases == null))
                mustardPurchases = new MustardPurchases(MainActivity.this, context);
        }
        catch (Exception ex)
        {
            debugLog.e("ERROR: ANDROID:","onStart mustardPurchases:" + ex.getMessage());
        }

        try {
            startLocationUpdates();
        }
        catch (Exception ex)
        {
            debugLog.e("ERROR: ANDROID:","onStart startLocationUpdates:" + ex.getMessage());
        }
        try {
            if (mWebView.getUrl() != null && mWebView.getUrl().endsWith("maintenance.html")) {
                htmlAPI.restartload();
            }
        }
        catch (Exception ex)
        {
            debugLog.e("ERROR: ANDROID:","onStart mWebView check:" + ex.getMessage());
        }

/*        WaitForFrontEnd waitForFrontEnd = new WaitForFrontEnd(this,context);
        waitForFrontEnd.RunIt(new WaitForFrontEnd.onReturnListener() {
            @Override
            public void onFinished(String result) {
                if (result != "OK")
                {
                    Log.d("ERROR: ANDROID:", "MainActivity waitForFrontEnd result: not ok ");
                    htmlAPI.showError("Failed to contact server");
                  //  dialog.setMessage("Failed to contact server");
                //    System.exit(0);
                } else {

                }

                mFinishedLoading = true;
            }
        } );*/

     //   new WaitForFrontEnd(this,context).execute("");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        try {
            AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
        }
        catch (Exception ex)
        {
            debugLog.e("ERROR: ANDROID:","onStart mGoogleApiClient getIndexApiAction:" + ex.getMessage());
        }

     //   long startTime = System.currentTimeMillis();
     //   try {
     //       do {

     //       } while (((System.currentTimeMillis()-startTime)<20000) && !mFinishedLoading);
    //    } catch (Exception e) {
     //       e.printStackTrace();
     //   }


    }

    protected void onStop() {
        mGoogleApiClient.disconnect();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(onEvent);

       // mustardPurchases.Dispose();
        stopLocationUpdates();
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        try {
            AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
        }
        catch (Exception ex)
        {
            debugLog.e( "ERROR", "Android: onStop err:" + ex.getMessage());
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        String lUrl = getResources().getString(R.string.mainDomain)+ "v" + ((appGlobal)this.getApplicationContext()).getAppVersion() + "/";
        if (BuildConfig.DEBUG) {
            lUrl =getResources().getString(R.string.mainDomain_debug)+ "v" + ((appGlobal)this.getApplicationContext()).getAppVersion() + "/";
        }
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse(lUrl + "index.html"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

/*    public void ShowDialogMessage(String sMessage)
    {
        dialog.setMessage(sMessage);
    }
    public void DismissDialogMessage()
    {
        dialog.dismiss();
    }*/

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    // [START signin]
    public void googleSignIn() {
   //     htmlAPI.showLoading();
        try {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        catch (Exception ex)
        {
            htmlAPI.hideLoading();
            debugLog.e( "ERROR", "Android: googleSignIn err:" + ex.getMessage());
        }
    }
    // [END signin]

    public void googleSignOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
    }
    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        debugLog.d("INFO: ANDROID:","MainActivity Google firebaseAuthWithGoogle getId:" + acct.getId());
        debugLog.d("INFO: ANDROID:","MainActivity Google firebaseAuthWithGoogle getIdToken:" + acct.getIdToken());
        // [START_EXCLUDE silent]
        htmlAPI.showLoading();
        // [END_EXCLUDE]
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            final String IDToken = acct.getIdToken();
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            try {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    debugLog.d("INFO: ANDROID:","MainActivity Google signInWithCredential:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    PwStorage hashing = new PwStorage();
                                    htmlAPI.googleLoginRtn(IDToken,hashing.main(((appGlobal) getApplication()).getAppDeviceId()));
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("INFO: ANDROID:","MainActivity Google signInWithCredential:failure", task.getException());
                                    htmlAPI.showError("Authentication failed.");
                                    htmlAPI.hideLoading();
                                }

                                // [START_EXCLUDE]

                                // [END_EXCLUDE]
                            }
                            catch (Exception ex)
                            {
                                htmlAPI.showError("We had a problem authenticating with Google:" + ex.getMessage());
                                htmlAPI.hideLoading();
                                debugLog.e("ERROR ANDROID","MainActivity: firebaseAuthWithGoogle2" + ex.getMessage());
                            }
                        }
                    });
        }
        catch (Exception ex) {
            htmlAPI.showError("We had a problem authenticating with Google:" + ex.getMessage());
            htmlAPI.hideLoading();
            debugLog.e("ERROR: ANDROID:","MainActivity: firebaseAuthWithGoogle" + ex.getMessage());
        }
    }
    // [END auth_with_google]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        debugLog.d("INFO: ANDROID:", "MainActivity onActivityResult: requestCode: " + requestCode);
        //mCallbackManager.onActivityResult(requestCode, resultCode, data);
        //mCallbackManager.onActivityResult(requestCode, resultCode, data);
        try {
            debugLog.d("INFO: ANDROID:", "MainActivity onActivityResult: requestCode: " + requestCode);
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                } else {
                    // Google Sign In failed, update UI appropriately
                    // [START_EXCLUDE]
                    debugLog.d("INFO: ANDROID", "MainActivity onActivityResult result:" + result.toString());
                    debugLog.d("INFO: ANDROID", "MainActivity onActivityResult result getStatus:" + result.getStatus());
                    htmlAPI.showError("Sorry, we had a problem authenticating with Google.");
                    htmlAPI.hideLoading();
                    //  updateUI(null);
                    // [END_EXCLUDE]
                }
            }
            else if (requestCode == RC_SAVEURL)
            {
                String pUrl = data.getStringExtra("Url");
                String pPageType = data.getStringExtra("PageType");
                if ((pUrl != "") && (pUrl != null)){
                    htmlAPI.SaveUrl(pUrl,pPageType);
                }
            }
            else if (requestCode == RC_SETTINGS)
            {
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:askForNotificationRtn('done');");
                    }
                });
            }
            else if (requestCode == mustardPurchases.RC_REQUEST) {

                debugLog.d("INFO: ANDROID:", "MainActivity onActivityResult: RC_REQUEST");

                if (mustardPurchases != null) {
                    // Pass on the activity result to the helper for handling
                    if (!mustardPurchases.handleActivityResult(requestCode, resultCode, data)) {
                        // not handled, so handle it ourselves (here's where you'd
                        // perform any handling of activity results not related to in-app
                        // billing...
                        //super.onActivityResult(requestCode, resultCode, data);
                    } else {
                        debugLog.d("INFO: ANDROID", "MainActivity onActivityResult handled by IABUtil.");
                    }
                } else {
                    debugLog.e("ERROR: ANDROID", "MainActivity onActivityResult mustardPurchases are null.");
                    htmlAPI.showError("Sorry, we had a problem communicating with Google.");
                }
            }
            else if (requestCode == RC_DROP_IN_REQUEST) {

                    DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                    PaymentMethodNonce paymentMethodNonce = result.getPaymentMethodNonce();
                    Exception error = null;
                    if ((resultCode != BT_RESULT_OK) && (resultCode != BT_RESULT_CANCELED)) error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);

                    mustardPurchases.handleBraintreeResult(resultCode,result, error);

            }
            else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
            } else {
                return;
            }


            debugLog.d("onactresults", "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        }
        catch (Exception ex)
        {
            debugLog.e("ERROR: ANDROID: ", "onActivityResult:" + requestCode + ":" + resultCode + " err:" + ex.getMessage());
        }

    }
//    public class MyJavaScriptInterface {
//
//        private Context ctx;
//        public String html;
//
//        MyJavaScriptInterface(Context ctx) {
//            this.ctx = ctx;
//        }
//
//        @JavascriptInterface
//        public void showHTML(String _html) {
//            html = _html;
//        }
//
//    }
    private BroadcastReceiver onEvent=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent i) {
            try {


                if (i.getStringExtra("title") != null) {
                    debugLog.d(TAG, "INFO: ANDROID: BroadcastReceiverMessage data payload: " + i.getStringExtra("title"));
                    debugLog.d(TAG, "INFO: ANDROID: BroadcastReceiverMessage data payload body: " + i.getStringExtra("body"));
                    if (i.getStringExtra("NotifyData") != null) {
                        debugLog.e("INFO: ANDROID: ", "MainActivity BroadcastReceiver NotifyData:" + i.getStringExtra("NotifyData"));
                        htmlAPI.showNotification(i.getStringExtra("NotifyData"), i.getStringExtra("title"), i.getStringExtra("body"));
                    } else {
                        htmlAPI.showNotification("", i.getStringExtra("title"), i.getStringExtra("body"));
                    }
                } else {
                    if (i.getStringExtra("NotifyData") != null) {
                        debugLog.e("INFO: ANDROID: ", "MainActivity BroadcastReceiver NotifyData2:" + i.getStringExtra("NotifyData"));
                        htmlAPI.NotificationDataRtn(i.getStringExtra("NotifyData"));
                    }
                }


            }
            catch (Exception ex)
            {
                debugLog.e("ERROR: ANDROID: ", "MainActivity BroadcastReceiver:" + ex.getMessage());
            }
        }
    };

    public void startLocationUpdates() {
        try {
            if (ContextCompat.checkSelfPermission(mWebView.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback,
                        null /* Looper */);
            }
        }
        catch (Exception ex) {
            debugLog.e("ERROR: ANDROID: ", "MainActivity startLocationUpdates:" + ex.getMessage());
        }
    }
    public void stopLocationUpdates() {
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        catch (Exception ex)
        {
            debugLog.e("ERROR: ANDROID: ", "MainActivity stopLocationUpdates:" + ex.getMessage());
        }
    }
    public boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }


}
