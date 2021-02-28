package com.example.myglass;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class GlassActivity extends AppCompatActivity {

    private TextView mMsg;
    private Button mStartButton;

    private BluetoothAdapter mAdapter;
    private int mBluetoothState;
    private Boolean mRunning;
    private ConnectedThread mCommunication;

    private final BroadcastReceiver mReceiverState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_OFF)
                    SocketHandler.setSocket(null);
                mBluetoothState = state;
                render();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass);

        mMsg = (TextView) findViewById(R.id.activity_glass_msg);
        mStartButton = (Button) findViewById(R.id.activity_glass_btn);

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mRunning = false;
        mCommunication = null;

        if (mAdapter == null)
            mMsg.setText("Bluetooth is not supported.");
        else {
            if (!mAdapter.isEnabled())
                mBluetoothState = BluetoothAdapter.STATE_OFF;
            else
                mBluetoothState = BluetoothAdapter.STATE_ON;

            render();

            IntentFilter filterState = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

            registerReceiver(mReceiverState, filterState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        if (mBluetoothState == BluetoothAdapter.STATE_OFF) {
            mMsg.setText(R.string.no_bluetooth);
            mStartButton.setVisibility(View.GONE);
        } else if (SocketHandler.getSocket() == null || !SocketHandler.getSocket().isConnected()) {
            mMsg.setText(R.string.glass_not_connected_msg);
            mStartButton.setVisibility(View.VISIBLE);
            mStartButton.setText(R.string.glass_bt_device_list_btn);
            mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent deviceListActivity = new Intent(GlassActivity.this, DeviceListActivity.class);

                    startActivity(deviceListActivity);
                    render();
                }
            });
        } else if (mRunning == false) {
            mMsg.setVisibility(View.VISIBLE);
            mMsg.setText("Press start to use MyGlass");
            mStartButton.setVisibility(View.VISIBLE);
            mStartButton.setText("Start");
            mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //mCommunication = new ConnectedThread(SocketHandler.getSocket());

                    mRunning = true;
                    render();
                    //mCommunication.run();
                }
            });
        } else {
            mMsg.setText("MyGlass is running...");
            mStartButton.setVisibility(View.VISIBLE);
            mStartButton.setText("Stop");
            mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRunning = false;
                    render();
                }
            });
        }
    }

    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket) {
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

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true) {
                try {
                    buffer[bytes] = (byte) mInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(String input) {
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

    public class CallReceiver extends PhonecallReceiver {

        @Override
        protected void onIncomingCallStarted(Context ctx, String number, Date start) {
            if (mCommunication != null)
                mCommunication.write("INCOMING_CALL_STARTED:" + number);
        }

        @Override
        protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            if (mCommunication != null)
                mCommunication.write("OUTGOING_CALL_STARTED:" + number);
        }

        @Override
        protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            if (mCommunication != null)
                mCommunication.write("INCOMING_CALL_ENDED:" + number);
        }

        @Override
        protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
            if (mCommunication != null)
                mCommunication.write("OUTGOING_CALL_ENDED:" + number);
        }

        @Override
        protected void onMissedCall(Context ctx, String number, Date start) {
            if (mCommunication != null)
                mCommunication.write("MISSED_CALL:" + number);
        }

    }
}