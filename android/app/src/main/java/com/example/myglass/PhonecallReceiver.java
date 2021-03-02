package com.example.myglass;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public abstract class PhonecallReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("PHONE", "Call");

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else {
            /*
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Log.e("PHONE CALL RECEIVER", "Number:" + number);
            int state = 0;

            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE))
                state = TelephonyManager.CALL_STATE_IDLE;
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING))
                state = TelephonyManager.CALL_STATE_RINGING;

            onCallStateChanged(context, state, number);

             */
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    Log.e("PHONE CALL RECEIVER", "Number" + phoneNumber);
                    onCustomCallStateChanged(context, state, phoneNumber);
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    protected void onIncomingCallStarted(Context ctx, String number, Date start){}
    protected void onOutgoingCallStarted(Context ctx, String number, Date start){}
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){}
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){}
    protected void onMissedCall(Context ctx, String number, Date start){}

    //public void onCallStateChanged(Context context, int state, String number) {
    public void onCustomCallStateChanged(Context context, int state, String number) {

        if(lastState == state)
            return;

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;

                onIncomingCallStarted(context, number, callStartTime);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                if(lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();

                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                if(lastState == TelephonyManager.CALL_STATE_RINGING)
                    onMissedCall(context, savedNumber, callStartTime);
                else if(isIncoming)
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                else
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                break;

        }
        lastState = state;
    }
}
