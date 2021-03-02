package com.example.myglass;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BlendMode;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
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
    private Button mCancelButton;

    private BluetoothAdapter mAdapter;
    private int mBluetoothState;
    private Boolean mRunning;

    private final BroadcastReceiver mReceiverState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_OFF)
                    BluetoothCommunication.setSocket(null);
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
        mCancelButton = (Button) findViewById(R.id.activity_glass_cancel_btn);

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mRunning = false;

        if (mAdapter == null)
            mMsg.setText("Bluetooth is not supported.");
        else {
            if (!mAdapter.isEnabled())
                mBluetoothState = BluetoothAdapter.STATE_OFF;
            else
                mBluetoothState = BluetoothAdapter.STATE_ON;

            render();

            IntentFilter filterState = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PHONE_STATE");
            intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");

            CallReceiver callReceiver = new CallReceiver();

            registerReceiver(callReceiver, intentFilter);
            registerReceiver(mReceiverState, filterState);
            Log.e("Receivers:", "Call receiver set.");
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
        } else if (BluetoothCommunication.getSocket() == null || !BluetoothCommunication.getSocket().isConnected()) {
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
                    mRunning = true;
                    render();

                    CommunicationThread communication = new CommunicationThread(BluetoothCommunication.getSocket());

                    BluetoothCommunication.setCommunicationThread(communication);
                    BluetoothCommunication.write("Activity started!");
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

                    BluetoothCommunication.write("Activity stopped, bye!");
                }
            });
        }
    }
}