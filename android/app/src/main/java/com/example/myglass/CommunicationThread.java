package com.example.myglass;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CommunicationThread extends Thread {
    private BluetoothSocket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;

    public CommunicationThread(BluetoothSocket socket) {
        mSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }

    public void write(String input) {
        Log.e("Send", "Sending this string" + input);
        byte[] bytes = input.getBytes();
        try {
            mOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("Send Error","Unable to send message",e);
        }
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) { }
    }
}