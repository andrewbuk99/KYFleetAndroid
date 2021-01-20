package com.mustardtec.yourfleet.util;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mustardtec.yourfleet.MainActivity;
import com.mustardtec.yourfleet.appGlobal;

/**
 * Created by danie on 29/09/2017.
 */

public class WaitForHTML extends AsyncTask<String, Integer, Integer> {
    //Context context;
    private MainActivity mAct;
    private Context mCon;
    private onReturnHTMLListener mListerner;
    public WaitForHTML(Activity act, Context con) {
        mAct = (MainActivity)act;
        mCon = con;

    }
    public interface onReturnHTMLListener {
        /**
         * Called to notify that setup is complete.
         *
         * @param result The result of the setup process.
         */
        void onFinished(String result);
    }

    public void RunIt(final onReturnHTMLListener listener) {
        mListerner = listener;
        execute("");

    }

    public void Cancel() {
        cancel(true);
    }


    protected Integer doInBackground(String... vname) {
        int notries = 300000;
        int retry = 0;
        int actualgoes = 0;
        String tempApp = "";
        boolean vfound = false;
        long startTime = System.currentTimeMillis();
        try {
            do {
                actualgoes++;
                tempApp = ((appGlobal) this.mAct.getApplication()).getHtmlStr();

                if (tempApp != "" && tempApp != null) {
                    Log.d("ANDROID: WaitForHTML", "appReady got: " + actualgoes);
 //                   Log.d("ANDROID: WaitForHTML", "appReady html: " + tempApp);
                    actualgoes=9999999;
                    vfound=true;
                }
            } while (((System.currentTimeMillis()-startTime)<60000) && !vfound && ((appGlobal) mAct.getApplication()).getHtmlRunningStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return actualgoes;
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        if (result == 9999999) {
            mListerner.onFinished("OK");
        } else {
            mListerner.onFinished("FAILED");
        }

    }
}