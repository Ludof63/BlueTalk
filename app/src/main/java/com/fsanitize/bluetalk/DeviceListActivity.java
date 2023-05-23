package com.fsanitize.bluetalk;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;

import java.util.Set;

@SuppressLint("MissingPermission")
public class DeviceListActivity extends BluetoothBaseActivity {

    private final static String LOG_TAG = "device-list-activity";
    private ListView listPairedDevices, listAvailableDevices;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private Button buttonScan;
    private ProgressBar progressBar_scan;
    private Context context;
    private Toolbar toolbar;
    private BluetoothConnector bluetoothConnector;

    private Handler returnBluetoothConnectorHandler = new Handler(new Handler.Callback() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_bluetooth_settings) {
            bluetoothSettings();
            return true;
        }
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onBackPressed() {
        bluetoothConnector.CancelListen();
        DeviceListActivity.super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        toolbar = findViewById(R.id.toolbar_device_list_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discover Nearby");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        listPairedDevices = findViewById(R.id.listview_paired_devices);
        adapterPairedDevices = new ArrayAdapter<>(this,R.layout.device_list_item);
        listPairedDevices.setAdapter(adapterPairedDevices);

        buttonScan = findViewById(R.id.button_scan);
        progressBar_scan = findViewById(R.id.progressBar_scan);
        context = this;

        listAvailableDevices = findViewById(R.id.listview_new_devices);
        adapterAvailableDevices = new ArrayAdapter<>(this,R.layout.device_list_item);
        listAvailableDevices.setAdapter(adapterAvailableDevices);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothConnector = new BluetoothConnector(bluetoothAdapter,returnBluetoothConnectorHandler);
        progressBar_scan.setActivated(false);
        progressBar_scan.setVisibility(View.INVISIBLE);

        bluetoothConnector.Listen();

        listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                bluetoothConnector.Connect(address);
            }
        });

        IntentFilter filterDiscoverFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterDiscoverEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoverReceiver, filterDiscoverFound);
        registerReceiver(discoverReceiver, filterDiscoverEnd);

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

        buttonScan.setOnClickListener(view -> scanDevices());

    }


    private void bluetoothSettings() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        startActivity(new Intent().setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(discoverReceiver);
    }

    private void scanDevices() {
        adapterAvailableDevices.clear();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
        progressBar_scan.setVisibility(View.VISIBLE);
        progressBar_scan.setActivated(true);
        showToast(this,"Scan started" );
    }



    private final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                showToast(context,"Device found " + device.getName());
//                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
//                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar_scan.setVisibility(View.INVISIBLE);
                progressBar_scan.setActivated(false);
                if (adapterAvailableDevices.getCount() == 0) {
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Click on the device to start the chat", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

}