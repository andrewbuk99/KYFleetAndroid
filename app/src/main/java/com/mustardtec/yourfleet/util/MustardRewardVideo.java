//package com.mustardtec.yourfleet.util;
//
//import android.content.Context;
//import android.os.Handler;
//import android.webkit.WebView;
//import android.widget.Toast;
//
//import com.google.android.gms.ads.reward.RewardItem;
//import com.google.android.gms.ads.reward.RewardedVideoAdListener;
//
///**
// * Created by danie on 17/11/2017.
// */
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.reward.RewardedVideoAd;
//import com.mustardtec.yourfleet.MainActivity;
//import com.mustardtec.yourfleet.R;
//import com.mustardtec.yourfleet.WebAPI;
//import com.mustardtec.yourfleet.appGlobal;
//
//public class MustardRewardVideo implements RewardedVideoAdListener {
//
//    private Context mCont;
//    private String mAndroidId = "";
//    private String mAuthToken = "";
//    private MainActivity mAct;
//    public RewardedVideoAd mRewardedVideoAd;
//
//    public MustardRewardVideo(MainActivity pAct, Context cont) {
//
//        mAct = pAct;
//        mAndroidId =  ((appGlobal) mAct.getApplication()).getAppDeviceId();
//        mAuthToken = ((appGlobal) mAct.getApplication()).getAuthToken();
//        mCont = cont;
//
//        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mAct);
//        mRewardedVideoAd.setRewardedVideoAdListener(this);
//
//      //  loadRewardedVideoAd();
//    }
//
//    public void loadRewardedVideoAd() {
//
//        Handler mainHandler = new Handler(mCont.getMainLooper());
//        mainHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                String appId = mCont.getResources().getString(R.string.adMobVideoID);
//                if (!mRewardedVideoAd.isLoaded()) {
//                    mAct.mustardRewards.mRewardedVideoAd.loadAd(appId,
//                            new AdRequest.Builder().build());
//                }
//            }
//        });
//
//    }
//    public void showRewardedVideoAd() {
//        final Handler mainHandler = new Handler(mCont.getMainLooper());
//        mainHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                if (mAct.mustardRewards.mRewardedVideoAd.isLoaded()) {
//                    mAct.mustardRewards.mRewardedVideoAd.show();
//                } else {
//                    mAct.htmlAPI.showError("Sorry, we don't have any videos for you yet. Keep checking");
//                    mAct.htmlAPI.hideLoading();
//                    loadRewardedVideoAd();
//
//                }
//            }
//        });
//
//    }
//
//    @Override
//    public void onRewarded(RewardItem reward) {
//
//        String[] sparams = {reward.getType(), "", "", "", "", ""};
//        WebAPI webAPI = new WebAPI(mCont);
//        webAPI.Timeout = 60000;
//        webAPI.Controller = "credits";
//        webAPI.Action = "AddCredit";
//        webAPI.RunIt(sparams, new WebAPI.onWebAPIReturnListener() {
//            public void onFinished(String result) {
//                try {
//                    mAct.debugLog.d("INFO: ANDROID:","MustardRewardVideo onRewarded after WEBAPI run:" + result);
//
//                    if (result.contains("OK"))
//                    {
//                        mAct.htmlAPI.doRewardVideoRtn("OK");
//                    } else {
//                        mAct.htmlAPI.doRewardVideoRtn("FAILED");
//                    }
//
//                } catch (Exception ex) {
//                    mAct.htmlAPI.doRewardVideoRtn("FAILED");
//                    ex.printStackTrace();
//                }
//            }
//
//        });       // Reward the user.
//    }
//
//    @Override
//    public void onRewardedVideoAdLeftApplication() {
//
//    }
//
//    @Override
//    public void onRewardedVideoAdClosed() {
//        // Preload the next video ad.
//        loadRewardedVideoAd();
//    }
//
//    @Override
//    public void onRewardedVideoAdFailedToLoad(int errorCode) {
//     //   mAct.htmlAPI.showNotification("Videos", "Sorry..something went wrong and we couldn't show you a video.");
//        mAct.debugLog.d("ERROR: ANDROID:","MustardRewardVideo onRewardedVideoAdFailedToLoad errorCode:" + errorCode);
//    }
//
//    @Override
//    public void onRewardedVideoAdLoaded() {
//    //    Toast.makeText(mAct, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRewardedVideoAdOpened() {
//    //    Toast.makeText(mAct, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRewardedVideoStarted() {
//        //    Toast.makeText(mAct, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
//    }
//}
