package com.example.myglass;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothCommunication {
    private static BluetoothSocket socket;
    private static CommunicationThread thread;

    public static synchronized BluetoothSocket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(BluetoothSocket socket){
        BluetoothCommunication.socket = socket;
    }

    public static synchronized CommunicationThread getCommunicationThread() {
        return thread;
    }

    public static synchronized void setCommunicationThread(CommunicationThread thread) {
        BluetoothCommunication.thread = thread;
    }

    public static synchronized void write(String input) {
        if (thread != null)
            thread.write(input);
    }
}
