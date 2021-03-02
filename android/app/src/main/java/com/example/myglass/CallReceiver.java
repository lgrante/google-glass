package com.example.myglass;

import android.content.Context;
import android.util.Log;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {
    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        BluetoothCommunication.write("Incoming call from " + number);
        Log.e("CALL", "Incoming call from ");
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        BluetoothCommunication.write("Outgoing call from " + number);
        Log.e("CALL", "Outgoing call");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        BluetoothCommunication.write("Incoming call ended with " + number);
        Log.e("CALL", "Incoming call ended");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        BluetoothCommunication.write("Outgoing call ended with " + number);
        Log.e("CALL", "Outgoing call ended");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        BluetoothCommunication.write("Missed call from " + number);
        Log.e("CALL", "Missed call");
    }
};