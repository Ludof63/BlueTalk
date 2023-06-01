package com.fsanitize.bluetalk.UI;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.fsanitize.bluetalk.Data.BlueTalkHistory;
import com.fsanitize.bluetalk.Logic.BluetoothChat;
import com.fsanitize.bluetalk.Logic.BluetoothConnector;
import com.fsanitize.bluetalk.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

//@SuppressLint("MissingPermission")
public class DeviceListActivity extends BluetoothBaseActivity {
    private final static String LOG_TAG = "device-list-activity";
    private final static int VISIBILITY_REQUEST_CODE = 22;

    Map<Integer, String> deviceTypes = new HashMap<Integer,String>() {
        {
            put(BluetoothClass.Device.PHONE_SMART, "OK");
        }
    };
    private static HashMap<String,BluetoothDevice> availabeDevices= new HashMap<> ();
    private ListView listAvailableDevices;
    private ArrayAdapter<String> adapterAvailableDevices;
    private Button buttonScan;
    private ProgressBar progressBar_scan;
    private Context context;
    private BluetoothConnector bluetoothConnector;
    private final BlueTalkHistory historyManager = new BlueTalkHistory();
    private boolean isConnecting;


    private final Handler returnBluetoothConnectorHandler = new Handler(new Handler.Callback() {

        @SuppressLint("MissingPermission")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            BluetoothSocket connectedSocket = null;
            Log.d(LOG_TAG, "Connector handler: received a result");
            switch (msg.what){
                case BluetoothConnector.ConnectorResult.CONNECTION_OK:
                    Log.d(LOG_TAG, "Connector handler: connection ok");
                    connectedSocket = (BluetoothSocket) msg.obj;
                    if (connectedSocket == null) {
                        Log.e(LOG_TAG, "Connector handler: Bluetooth socket is null");
                        return false;
                    }
                    bluetoothChat = new BluetoothChat(connectedSocket);
                    startActivity(new Intent(context, BluetoothChatActivity.class));
                    break;
                case BluetoothConnector.ConnectorResult.CONNECTION_TIMEOUT:
                    Log.d(LOG_TAG, "Connector handler: connection ko");
                    showToast(context,"BlueTalk connection failed");
                    isConnecting = false;
                    break;
                case BluetoothConnector.ConnectorResult.CONNECTION_RECEIVED:
                    Log.d(LOG_TAG, "Connector handler: accepted connection, sure?");
                    BluetoothSocket acceptedSocket= (BluetoothSocket) msg.obj;
                    if (acceptedSocket == null) {
                        Log.e(LOG_TAG, "Connector handler: Bluetooth socket is null");
                        return false;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);
                    builder.setTitle(R.string.received_connection);
                    builder.setMessage(getString(R.string.do_you_want_to_bluetalk_with) + acceptedSocket.getRemoteDevice().getName() + "? ");
                    builder.setPositiveButton(getString(R.string.app_name), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(LOG_TAG,"Connector handler: connection accepted started chat");
                            bluetoothChat = new BluetoothChat(acceptedSocket);
                            try {
                                OutputStream ack_stream = acceptedSocket.getOutputStream();
                                ack_stream.write(BluetoothConnector.ACK);
                                Log.d(LOG_TAG,"Connector handler: connection accepted ACK sent");
                            } catch (IOException e) {
                                Log.e(LOG_TAG,"Connector handler: error sending ACK");
                                try {
                                    acceptedSocket.close();
                                } catch (IOException ex) {
                                    Log.e(LOG_TAG,"Connector handler: could not close the socket after ACK failed");
                                }
                            }
                            startActivity(new Intent(context, BluetoothChatActivity.class));
                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(LOG_TAG,"Connector handler: connection refused");
                            try {
                                acceptedSocket.close();
                            } catch (IOException e) {
                                Log.e(LOG_TAG,"Connector handler: could not close the socket");
                            }
                        }
                    });
                    builder.show();
                    break;
            }
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
        if (id == R.id.action_location_settings) {
            locationSettings();
            return true;
        }
        if (id == R.id.action_visibility) {
            bluetoothVisibility();
            return true;
        }
        if (id == android.R.id.home) {
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
        context = this;

        Toolbar toolbar = findViewById(R.id.toolbar_device_list_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.discover_nearby);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonScan = findViewById(R.id.button_scan);
        progressBar_scan = findViewById(R.id.progressBar_scan);

        listAvailableDevices = findViewById(R.id.listview_new_devices);
        adapterAvailableDevices = new ArrayAdapter<>(this, R.layout.device_list_item);
        listAvailableDevices.setAdapter(adapterAvailableDevices);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isConnecting = false;
        setProgressBar(false);

        //register receiver for discover
        registerReceiver(discoverReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(discoverReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(discoverReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));

        bluetoothConnector = new BluetoothConnector(bluetoothAdapter, returnBluetoothConnectorHandler);
        bluetoothConnector.Listen();

        listAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isConnecting){
                    showToast(context,"Wait...connection already in progress");
                }else {
                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length() - 17);
                    bluetoothConnector.Connect(availabeDevices.get(address));
                    showToast(context, getString(R.string.trying_to_start_bluetalk_connection));
                    isConnecting = true;
                }
            }
        });

        buttonScan.setOnClickListener(view -> scanDevices());
    }

    @Override
    protected void handlerBluetoothIsEnabled() {
    }

    @Override
    protected void handlerBluetoothIsDisabled() {
        bluetoothConnector.CancelListen();
        DeviceListActivity.super.onBackPressed();
    }

    @SuppressLint("MissingPermission")
    private void bluetoothSettings() {
        stopScan();
        startActivity(new Intent().setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
    }

    private void locationSettings() {
        stopScan();
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    @Override
    protected void onStop() {
        super.onStop();
        historyManager.storeHistory(context);
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(discoverReceiver);
    }

    private void scanDevices() {
        //asking for background access
        if(ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            locationPermissionLauncher.launch(ACCESS_BACKGROUND_LOCATION);
            return;
        }

        //asking for location enabled
        if(!isLocationEnabled()){
            locationSettings();
            return;
        }

        adapterAvailableDevices.clear();
        availabeDevices.clear();

        stopScan();
        bluetoothAdapter.startDiscovery();

        progressBar_scan.setVisibility(View.VISIBLE);
        progressBar_scan.setActivated(true);
    }

    @SuppressLint("MissingPermission")
    private void stopScan(){
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }


    @SuppressLint("MissingPermission")
    private final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothClass device_class = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                if(device.getName() !=  null)
                    Log.d(LOG_TAG,"Device found " + device.getName());

                //only phones
                if(deviceTypes.containsKey(device_class.getDeviceClass())) {
                    //only not already founded (with name not null)
                    if (!availabeDevices.containsKey(device.getAddress()) && device.getName() != null) {
                        if(availabeDevices.isEmpty()){
                            showToast(context,getString(R.string.click_on_the_device_to_start_the_chat));
                        }
                        availabeDevices.put(device.getAddress(), device);
                        String status = "";
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED)
                            status = "[paired]\n";
                        adapterAvailableDevices.add(device.getName() + "\n" + status + device.getAddress());
                        adapterAvailableDevices.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar_scan.setVisibility(View.INVISIBLE);
                progressBar_scan.setActivated(false);

                if (availabeDevices.isEmpty())
                    showToast(context,getString(R.string.no_new_devices_found));


            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(LOG_TAG,"Discovery started");
            }
        }
    };

    private void setProgressBar(boolean enabled){
        progressBar_scan.setActivated(enabled);
        progressBar_scan.setVisibility(enabled?View.VISIBLE:View.INVISIBLE);
    }

    private boolean isLocationEnabled(){
        LocationManager loc = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        return loc.isLocationEnabled();
    }

    private ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if(isGranted){
            showToast(this, getString(R.string.background_location_granted));
            this.recreate();
        }
        else{
            showToast(this, getString(R.string.background_location_not_granted_needed_to_scan));
            finish();
        }
    });

    @SuppressLint("MissingPermission")
    private void bluetoothVisibility(){
        Log.d(LOG_TAG,"bluetooth visibility");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, VISIBILITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == VISIBILITY_REQUEST_CODE){
            if(resultCode == RESULT_CANCELED)
                Log.d(LOG_TAG,"The user denied discoverability");
            else
                showToast(context, getString(R.string.your_device_is_now_visible));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}