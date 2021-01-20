package com.mustardtec.yourfleet.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.content.IntentFilter;

import com.mustardtec.yourfleet.DebugLog;
import com.mustardtec.yourfleet.MainActivity;
import com.mustardtec.yourfleet.WebAPI;
import com.mustardtec.yourfleet.appGlobal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;

import static com.mustardtec.yourfleet.MainActivity.BT_RESULT_CANCELED;
import static com.mustardtec.yourfleet.MainActivity.BT_RESULT_OK;


/**
 * Created by dan on 22/09/2017.
 */

public class MustardPurchases {
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;

    DebugLog errLog = null;
    static public final int RC_REQUEST = 10001;

    private MainActivity mAct;
    private Context mCont;
    private onPurchaseCompleteListener mListerner;
    private onBraintreeFinishedListener mBraintreeListerner;
    // Product to in-app purchase
    public String SKU_Product;
    public String ProductName;
    public boolean mDisposed = false;

    // type of Purchase (ie restore or purchase)
    public String PaymentMode;

    public boolean mIsSetUp = false;

    // type of Product (ie subscription or normal product)
    public String ProductType;

    // Front end HTML function that will be called post process (this is provided by the front end that starts this purchase)
    public String FinalFunctionToUse;

    public String AndroidId = "";
    public String UserId = "";
    public String HTML5Version = "";


    public MustardPurchases(Activity act, Context cont) {
        mAct = (MainActivity)act;
        mCont = cont;
        AndroidId = ((appGlobal) this.mAct.getApplication()).getAppDeviceId();
        UserId = ((appGlobal) this.mAct.getApplication()).getAppUserID();
        HTML5Version = ((appGlobal) this.mAct.getApplication()).getHTML5Version();
        errLog = new DebugLog(cont);
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjepoJzLApFrdrwJXmz6s8AiKBhG5zVduZ5xOYzxFhwDnzsLzFeAxPPNeilapNPXVID0QTxd97GEugCV0MLAe0+ysa9ewS51qFa1Y4Weo2d3L2OAExO/DjnL5aRrYgeQL3Mn+JJb6gQixkjnxSt5Zp1fqmv7onzBiSMAOaAByaHimI5rEKRYXRio5wDFp83AiuOXsl7PEFfaIgeqkQ/GZC267Ki853x42gYRkZirg7XVcUSew3athdQaY8LuT76gNk6r3ZUbwz9ucNkmew9Ibv2ag4VhVe8wl0jrV9YNGXN1wauZ2DMF+uzpT21gd6OYaC66IACA34rrggEyWDHrF0wIDAQAB";
        mHelper = new IabHelper(mAct, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.errLog = errLog;
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                errLog.d("INFO: ANDROID:", "billingstart Setup finished.");


                if (!result.isSuccess()) {
                    mIsSetUp = false;
                    // Oh noes, there was a problem.
                    //complain("Problem setting up in-app billing: " + result);
                    return;
                } else {
                    mIsSetUp = true;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null)  {
                    mIsSetUp = false;
                    return;
                }

                mHelper.mPaymentMode = PaymentMode;
                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(new IabBroadcastReceiver.IabBroadcastListener()
                {
                    public void receivedBroadcast() {
                    }

                });

                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                mAct.registerReceiver(mBroadcastReceiver, broadcastFilter);
            }
        });
    }

    public interface onPurchaseCompleteListener {
        /**
         * Called to notify that setup is complete.
         *
         * @param result The result of the setup process.
         */
        void onFinished(String result);
    }

    public void Dispose() {
        try {
            mDisposed = true;
            mHelper.dispose();
            mAct.unregisterReceiver(mBroadcastReceiver);

        }
        catch (Exception ex)
        {

        }

    }
    public void Buy(String p_Product, String p_ReturnHTMLFunction, String p_ProductName, final onPurchaseCompleteListener listener) {

        SKU_Product = p_Product;
        FinalFunctionToUse = p_ReturnHTMLFunction;
        ProductName = p_ProductName;

        String result = "";
        mListerner = listener;
        mHelper.mPaymentMode = PaymentMode;
        mHelper.queryInventoryAsync(mGotInventoryListener);

    }

    public void BuyWithBraintree(String pToken, String pAmt, String p_Product, String p_ReturnHTMLFunction, final onBraintreeFinishedListener listener) {

        SKU_Product = p_Product;
        FinalFunctionToUse = p_ReturnHTMLFunction;
        ProductName = p_Product;

        String result = "";
        mBraintreeListerner = listener;
        DropInRequest dropInRequest = new DropInRequest()
                .amount(pAmt)
                .clientToken(pToken);
        mAct.startActivityForResult(dropInRequest.getIntent(mAct), mAct.RC_DROP_IN_REQUEST);

    }

    public interface onBraintreeFinishedListener {
        /**
         * Called to notify that an Braintree purchase completed.
         *
         * @param result The result of the operation.
         */
        void onFinished(String Status, String result, String extraData, String error);
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            errLog.d("INFO: ANDROID: ", "querylistener Query inventorfy finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) {
                errLog.e("ERROR:" , "Android: MustardPurchases QueryInventoryFinishedListener1:");
                mListerner.onFinished("FAILED");
                return;
            }
            //mHelper.consumeAsync(inventory.getPurchase(SKU_Product),null);
            // Is it a failure?
            if (result.isFailure()) {
                errLog.e("ERROR:" , "Android: MustardPurchases QueryInventoryFinishedListener2:");
                mListerner.onFinished("FAILED");
                return;
            }
            if (inventory.hasPurchase(SKU_Product)) {
                mHelper.consumeAsync(inventory.getPurchase(SKU_Product),mConsumeFinishedListener);
            } else {

                String vPurchaseOrderId = "none";
                String vOrderTime = "none";
                String vMessage = "";
                String vToken = "";

                errLog.d("INFO: ANDROID: ", " querylistener Query inventory was successful.");
                Purchase products = inventory.getPurchase(SKU_Product);
                int purchaseState = 9;
                try {
                    if (products != null)
                        purchaseState = products.getPurchaseState();
                } catch (Exception e) {
                    errLog.e("ERROR:", "Android: MustardPurchases QueryInventoryFinishedListener3 ex:" + e.getMessage());
                }
                try {
                    if (products != null) {
                        vPurchaseOrderId = products.getOrderId();
                        vOrderTime = String.valueOf(products.getPurchaseTime());
                        vToken = products.getToken();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(products.getPurchaseTime());
                        calendar.add(Calendar.MONTH, 1);
                        Calendar nowDate = Calendar.getInstance();
                        if (calendar.before(nowDate)) {
                            purchaseState = 3; // cancelled
                        }
                    }
                } catch (Exception ex) {
                    errLog.e("ERROR: ANDROID:", "Mustardpurchases QueryInventoryFinishedListener:" + ex.getMessage());
                    mListerner.onFinished("ERROR:" + ex.getMessage());
                    return;
                }


                if ((products == null) || (purchaseState > 1) || (mHelper.mPaymentMode == "normal")) {
                    if (mHelper.mPaymentMode == "normal") {
                        mHelper.launchPurchaseFlow(mAct, SKU_Product, RC_REQUEST, mPurchaseFinishedListener, "");
                    } else {
                        errLog.d("INFO: ANDROID:", "Mustardpurchases QueryInventoryFinishedListener 4:");
                        Handler mainHandler = new Handler(mCont.getMainLooper());
                        mainHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO
                                //     htmlAPI.doRestoreRtn("FAILED");
                            }
                        });
                    }
                }
            }
        }
    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (( result != null) && (purchase != null))
                errLog.d("INFO: ANDROID:", "Mustardpurchases onpurchasefinished Purchase finished: " + result + ", purchase: " + purchase);
            else if (purchase != null) {
                errLog.d("INFO: ANDROID:", "Mustardpurchases onpurchasefinished Purchase finished: result is null, purchase: " + purchase);
            }
            else
            {
                errLog.d("INFO: ANDROID:", "Mustardpurchases onpurchasefinished Purchase finished: result " + result + ", purchase is null");

            }
            // if we were disposed of in the meantime, quit.
            String vPurchaseOrderId = "none";
            String vOrderTime = "none";
            String vMessage = "";
            String vToken = "";

            if (purchase != null){
                vPurchaseOrderId = purchase.getOrderId();
                vOrderTime = String.valueOf(purchase.getPurchaseTime());
                vToken = purchase.getToken();
            }
            if (result != null) {
                vMessage = result.getMessage();
            }

            if (mHelper == null) {
                errLog.e("ERROR: ANDROID:", "Mustardpurchases mPurchaseFinishedListener1:");
                mListerner.onFinished("FAILED");
                return;
            }

            if (result.isFailure()) {
                if (result.getMessage().indexOf("User canceled") > -1)
                    mListerner.onFinished("CANCELED");
                else
                    mListerner.onFinished("FAILED");
                errLog.e("ERROR: ANDROID:", "Mustardpurchases onpurchasefinished Could process the purchase:" + vMessage);
                return;
            }

            if (vPurchaseOrderId == "none") {
                errLog.e("ERROR: ANDROID:", "Mustardpurchases mPurchaseFinishedListener2:");
                mListerner.onFinished("FAILED");
                return;
            }

            if (PaymentMode == "normal") {
                JSONObject jsonParam;
                jsonParam = new JSONObject();
                try {
                    jsonParam.put("PurchaseOrderId", vPurchaseOrderId);
                    jsonParam.put("Token", vToken);
                    jsonParam.put("OrderTime", vOrderTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                errLog.e("INFO: ANDROID:", "Mustardpurchases onpurchasefinished onpurchasefinished Purchase successful.");
                // we consume our purchases straight away to buy Credits in the app
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            } else {
                errLog.e("ERROR: ANDROID:", "Mustardpurchases mPurchaseFinishedListener should be here: Payment mode:" + PaymentMode);
            }


        }
    };




    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (purchase != null)
                errLog.d("INFO: ANDROID:", "Mustardpurchases mConsumeFinishedListener Consumption finished. Purchase: " + purchase + ", result: " + result);
            else
                errLog.d("INFO: ANDROID:", "Mustardpurchases mConsumeFinishedListener Consumption finished. Purchase: null, result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) {
                errLog.d("INFO: ANDROID:", "Mustardpurchases mConsumeFinishedListener helper is null");
                return;
            }

            String vPurchaseOrderId = "none";
            String vOrderTime = "none";
            String vMessage = "";
            String vToken = "";

            if (purchase != null){
                vPurchaseOrderId = purchase.getOrderId();
                vOrderTime = String.valueOf(purchase.getPurchaseTime());
                vToken = purchase.getToken();
            } else {
            }


            if (result.isSuccess()) {
                JSONObject jsonParam;
                jsonParam = new JSONObject();
                try {
                    jsonParam.put("PurchaseOrderId", vPurchaseOrderId);
                    jsonParam.put("Token", vToken);
                    jsonParam.put("OrderTime", vOrderTime);
                } catch (JSONException e) {
                    errLog.e("ERROR: ANDROID:", "Mustardpurchases mConsumeFinishedListener 2 error creating json:");
                    e.printStackTrace();
                }

                String[] sparams = {"paid", SKU_Product, jsonParam.toString(), ProductName, "", ""};
                WebAPI webAPI = new WebAPI(mCont);
                webAPI.Timeout = 60000;
                webAPI.Controller = "Payments";
                webAPI.Action = "AndroidPaid";
                webAPI.RunIt(sparams, new WebAPI.onWebAPIReturnListener() {
                    public void onFinished(String result) {
                        try {
                            errLog.d("INFO: ANDROID", "onPaymentWebAPIReturn result:" + result);

                            String retVal = "OK";
                            if (!result.contains("yes")) retVal = "FAILED";

                            mHelper.mPaymentStatus = retVal;

                            mListerner.onFinished(retVal);


                            errLog.d("INFO: ANDROID","onPaymentWebAPIReturn  mHelper.mPaymentStatus:" + mHelper.mPaymentStatus);

                        } catch (Exception ex) {
                            mListerner.onFinished("FAILED");
                        }
                    }

                });

                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d("onpurchasefinished", "Consumption successful. Provisioning.");
            }
            else {
                errLog.e("ERROR: ANDROID:", "Mustardpurchases mConsumeFinishedListener purchase is not successful:");
            }

            Log.d("onpurchaseconsume", "End consumption flow.");
        }
    };


    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {

        // Pass on the activity result to the helper for handling
        return mHelper.handleActivityResult(requestCode, resultCode, data) ;

    }

    public boolean handleBraintreeResult(int resultCode, DropInResult result, Exception error)
    {
        PaymentMethodNonce paymentMethodNonce = result.getPaymentMethodNonce();
        String extraData = result.getDeviceData();
        if (resultCode == BT_RESULT_OK) {
            mBraintreeListerner.onFinished("OK", paymentMethodNonce.getNonce(), extraData, "");
        } else if (resultCode == BT_RESULT_CANCELED) {
            mBraintreeListerner.onFinished("CANCEL","",extraData, "");
        } else {
            mBraintreeListerner.onFinished("ERROR","", extraData, error.getMessage());
        }

        return true;

    }
}
