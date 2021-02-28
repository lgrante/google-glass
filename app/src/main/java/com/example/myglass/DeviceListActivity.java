package com.example.myglass;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceListActivity extends AppCompatActivity {

    private final String TAG = "DeviceListActivity";

    private TextView mMsg;
    private ListView mPairedDeviceList;
    private ListView mDeviceList;
    private TextView mPairedDevicesTitle;
    private TextView mDevicesTitle;

    private BluetoothAdapter mAdapter;
    private List<BluetoothDevice> mPairedDevices;
    private List<BluetoothDevice> mDevices;

    private int mBluetoothState;

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

    private final BroadcastReceiver mReceiverDiscovery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBluetoothState != BluetoothAdapter.STATE_ON)
                return;

            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!mDevices.contains(device))
                    mDevices.add(device);
                render();
            }
        }
    };

    private final BroadcastReceiver mReceiverRestartDiscovery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBluetoothState != BluetoothAdapter.STATE_ON)
                return;

            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mAdapter.cancelDiscovery();
                mAdapter.startDiscovery();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mPairedDeviceList = (ListView) findViewById(R.id.activity_list_paired_device_list);
        mPairedDevicesTitle = (TextView) findViewById(R.id.activity_device_list_paired_device_title);
        mDeviceList = (ListView) findViewById(R.id.activity_list_device_list);
        mDevicesTitle = (TextView) findViewById(R.id.activity_device_list_device_title);
        mMsg = (TextView) findViewById(R.id.activity_device_list_msg);

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevices = new ArrayList<>();
        mDevices = new ArrayList<>();

        if (mAdapter == null)
            mMsg.setText("Bluetooth is not supported.");
        else {
            if (!mAdapter.isEnabled())
                mBluetoothState = BluetoothAdapter.STATE_OFF;
            else
                mBluetoothState = BluetoothAdapter.STATE_ON;

            render();
            mAdapter.startDiscovery();
            setupBroadcastReceivers();
            setupDeviceListsCallbacks();
        }
    }

    private void setupBroadcastReceivers() {
        IntentFilter filterState = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter filterDiscovery = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterRestartDiscovery = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiverState, filterState);
        registerReceiver(mReceiverDiscovery, filterDiscovery);
        registerReceiver(mReceiverRestartDiscovery, filterRestartDiscovery);
    }

    private void setupDeviceListsCallbacks() {
        final Context context = this;
        mPairedDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                BluetoothDevice device = mPairedDevices.get(position);
                ConnectThread bluetoothConnect = new ConnectThread(mAdapter, device.getAddress(), view);

                bluetoothConnect.run();
            }
        });
    }

    private void render() {
        updateDeviceList();
        updateContent();
    }

    private void updateDeviceList() {
        if (mBluetoothState == BluetoothAdapter.STATE_OFF) {
            mPairedDevices.clear();
            mDevices.clear();
        } else
            mPairedDevices = new ArrayList<>(mAdapter.getBondedDevices());

        ArrayAdapter adapterPaired = new ArrayAdapter(this, R.layout.device_item, getDeviceNames(mPairedDevices));
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.device_item, getDeviceNames(mDevices));

        mPairedDeviceList.setAdapter(adapterPaired);
        mDeviceList.setAdapter(adapter);
    }

    private void updateContent() {
        Boolean showPairedDeviceTitle = mPairedDevices != null && mPairedDevices.size() > 0 && mBluetoothState == BluetoothAdapter.STATE_ON;
        Boolean showDeviceTitle = mDevices != null && mDevices.size() > 0 && mBluetoothState == BluetoothAdapter.STATE_ON;

        mPairedDevicesTitle.setVisibility(showPairedDeviceTitle ? View.VISIBLE : View.GONE);
        mDevicesTitle.setVisibility(showDeviceTitle ? View.VISIBLE : View.GONE);

        switch (mBluetoothState) {
            case BluetoothAdapter.STATE_OFF:
                mAdapter.cancelDiscovery();
                mMsg.setText(R.string.no_bluetooth);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mMsg.setText("Turning Bluetooth off...");
                break;
            case BluetoothAdapter.STATE_ON:
                mAdapter.startDiscovery();
                mMsg.setText("Select the MyGlass device");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                mMsg.setText("Turning Bluetooth on...");
                break;
        }
    }

    private List<String> getDeviceNames(List<BluetoothDevice> devices) {
        List<String> result = new ArrayList<String>();

        for (BluetoothDevice device : devices)
            result.add(getDeviceName(device));
        return result;
    }

    private String getDeviceName(BluetoothDevice device) {
        String name = device.getName();

        if (name == null)
            name = device.getAddress();
        return name;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiverState);
        unregisterReceiver(mReceiverDiscovery);
        unregisterReceiver(mReceiverRestartDiscovery);
    }

    public class ConnectThread extends Thread {
        private BluetoothSocket mSocket;
        private TextView mDeviceItemText;
        private BluetoothDevice mDevice;

        public ConnectThread(BluetoothAdapter adapter, String address, View deviceItem) {
            BluetoothDevice device = adapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;

            mDevice = device;
            mDeviceItemText = deviceItem.findViewById(R.id.label);

            String name = device.getName() == null ? device.getAddress() : device.getName();
            UUID uuid = device.getUuids()[0].getUuid();

            setDeviceItemText(name + " connecting...", R.color.primary);
            Log.e("ConnectThread", "Trying to connect to " + device.getName());

            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e("ConnectThread", "Failed to create socket:", e);
            }

            mSocket = tmp;
        }

        @SuppressLint("ResourceAsColor")
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            String name = mDevice.getName() == null ? mDevice.getAddress() : mDevice.getName();

            adapter.cancelDiscovery();
            try {
                mSocket.connect();
                Log.e("ConnectThread", "Device connected");
            } catch (IOException connectException) {
                try {
                    mSocket.close();

                    setDeviceItemText(name, R.color.transparent);
                    Log.e("ConnectThread", "Cannot connect to device:", connectException);
                } catch (IOException closeException) {
                    Log.e("ConnectThread", "Cannot close the socket:", closeException);
                }
                return;
            }

            setDeviceItemText(name + " connected", R.color.primary);
            Log.e("ConnectThread", "Starting communication...");

            SocketHandler.setSocket(mSocket);
            //ConnectedThread connectedThread = new ConnectedThread(mSocket);
            //connectedThread.run();
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e("ConnectThread", "Could not close the client socket", e);
            }
        }

        private void setDeviceItemText(String itemText, int color) {
            mDeviceItemText.post(new Runnable() {
                @Override
                public void run() {
                    mDeviceItemText.setText(itemText);
                    mDeviceItemText.setTextColor(color);
                }
            });
        }
    }
}