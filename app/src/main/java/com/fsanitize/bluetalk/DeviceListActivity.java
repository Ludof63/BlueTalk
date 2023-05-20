package com.fsanitize.bluetalk;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Set;

@SuppressLint("MissingPermission")
public class DeviceListActivity extends BluetoothBaseActivity {

    private final static String LOG_TAG = "device-list-activity";
    private ListView listPairedDevices;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private Button buttonScan;
    private Context context;

    private BluetoothConnector bluetoothConnector;

    private final Handler returnBluetoothConnectorHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Log.d(LOG_TAG,"Connector handler received a result");

            BluetoothSocket connectedSocket = (BluetoothSocket) msg.obj;
            if(connectedSocket == null){
                Log.e(LOG_TAG,"Bluetooth socket is null");
            }

            bluetoothChat = new BluetoothChat(connectedSocket);
            startActivity(new Intent(context,BluetoothChatActivity.class));
            Log.d(LOG_TAG,"Connector handler: end");
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        listPairedDevices = findViewById(R.id.paired_devices);
        buttonScan = findViewById(R.id.button_scan);
        context = this;

        adapterPairedDevices = new ArrayAdapter<>(this,R.layout.device_list_item);

        listPairedDevices.setAdapter(adapterPairedDevices);

        listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                bluetoothConnector.Connect(address);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothConnector = new BluetoothConnector(bluetoothAdapter,returnBluetoothConnectorHandler);

        bluetoothConnector.Listen();
    }

    @Override
    protected void handlerBluetoothIsEnabled() {

    }

    @Override
    protected void handlerBluetoothIsDisabled() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapterPairedDevices.clear();

        //already paired
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }

        buttonScan.setOnClickListener(view -> pairNewDevice());

    }


    private void pairNewDevice() {
        startActivity(new Intent().setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void scanDevices() {
        adapterAvailableDevices.clear();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
        showToast(this,"Scan started" );
    }



//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                showToast("Device found " + device.getName());
//                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
//                }
//            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                if (adapterAvailableDevices.getCount() == 0) {
//                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(context, "Click on the device to start the chat", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    };

}