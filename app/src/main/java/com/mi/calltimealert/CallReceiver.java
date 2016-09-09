package com.mi.calltimealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {

    static boolean itsOn = false;
    static boolean itsInComing = false;

    static Context context;

    @Override
    public void onReceive(Context mContext, Intent intent) {

        context = mContext;
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            System.out.println("CallReceiver.onReceive : " + "ACTION_NEW_OUTGOING_CALL");
        }

        Bundle bundle = intent.getExtras();

        if (!bundle.containsKey(TelephonyManager.EXTRA_STATE)) {
            return;
        }

        if (bundle.getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            System.out.println("CallReciever.onReceive : " + "EXTRA_STATE_RINGING");
            // Phone number
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            itsInComing = true;
            if (itsOn)
                CallWidget.startPlay(context);
            // Ringing state
            // This code will execute when the phone has an incoming call
        } else if (bundle.getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            // This code will execute when the call is answered or disconnected
            System.out.println("CallReciever.onReceive : " + "EXTRA_STATE_IDLE");
            removeCountDown();
            CallWidget.pause();
            itsInComing = false;
        } else if (bundle.getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            System.out.println("CallReciever.onReceive : " + "EXTRA_STATE_OFFHOOK");

            if (itsInComing) return;

            if (countDownTimer1 == null) {
                countDownTimer1 = new CountDownTimer(milli_minute * 3, milli_second_10) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        log("ON TICK1==" + millisUntilFinished);
//            startPlay();
                        if (itsOn)
                            CallWidget.startPlay(context);
                    }

                    @Override
                    public void onFinish() {
                        log("ON TICK1==FINISHED");
                        countDownTimer2.start();
                    }
                };
                countDownTimer1.start();
                log("ON TICK1==START");
            }
        }

    }


    void removeCountDown() {
        if (countDownTimer1 != null)
            countDownTimer1.cancel();
        if (countDownTimer2 != null)
            countDownTimer2.cancel();
        if (countDownTimer3 != null)
            countDownTimer3.cancel();

        countDownTimer1 = null;
    }

    public static void startlisten(Context context) {
        itsOn = true;


    }

    public static void stopListen() {
        itsOn = false;
        CallWidget.pause();
    }


    private static final String TAG = "CallReciever: ";
    private static final long milli_second = 1000;
    private static final long milli_second_10 = 1000 * 10;
    private static final long milli_minute = 1000 * 60;


    static CountDownTimer countDownTimer1;
    static CountDownTimer countDownTimer2 = new CountDownTimer(milli_minute * 1, milli_minute * 1) {
        @Override
        public void onTick(long millisUntilFinished) {
            log("ON TICK2==" + millisUntilFinished);
            if (itsOn)
                CallWidget.startPlay(context);
        }

        @Override
        public void onFinish() {
            countDownTimer3.start();
        }
    };

    static CountDownTimer countDownTimer3 = new CountDownTimer(milli_minute * 2, milli_minute * 2) {
        @Override
        public void onTick(long millisUntilFinished) {
            log("ON TICK3==" + millisUntilFinished);

            if (itsOn)
                CallWidget.startPlay(context);
        }

        @Override
        public void onFinish() {
            countDownTimer3.start();
        }
    };


    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    private void makeToast(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}