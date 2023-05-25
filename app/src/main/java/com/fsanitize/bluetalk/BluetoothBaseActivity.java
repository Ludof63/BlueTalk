package com.fsanitize.bluetalk;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_ADVERTISE;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.Serializable;

public abstract class BluetoothBaseActivity extends AppCompatActivity {
    private static final String[] REQUIRED_PERMISSIONS;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS =
                    new String[] {
                            BLUETOOTH_SCAN,
                            BLUETOOTH_ADVERTISE,
                            BLUETOOTH_CONNECT,
                            ACCESS_FINE_LOCATION
                    };
        } else {
            REQUIRED_PERMISSIONS =
                    new String[]{
                            BLUETOOTH,
                            BLUETOOTH_ADMIN,
                            ACCESS_FINE_LOCATION,
                    };
        }
    }

    private static final int REQUEST_PERMISSION = 11;
    protected static final String CONNECTED_SOCKET = "connected_docket";
    public static final String MY_ADDRESS = "me";
    protected static BluetoothManager bluetoothManager;
    protected static BluetoothAdapter bluetoothAdapter;
    protected static BluetoothChat bluetoothChat = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            showToast(this, "Bluetooth is not available on this device");
            return;
        }

        getBluetoothPermissions();
        registerReceiver(bStateReceiver,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }



    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(bStateReceiver);
    }

    private void getBluetoothPermissions() {
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSION);
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            int i = 0;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    showToast(this, "Permission not granted: " + permissions[i]);
                    finish();
                    return;
                }
                i++;
            }
            recreate();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    private final BroadcastReceiver bStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    handlerBluetoothIsDisabled();
                }
                else{
                    handlerBluetoothIsEnabled();
                }
            }
        }

    };

    protected abstract void handlerBluetoothIsEnabled();
    protected abstract void handlerBluetoothIsDisabled();

    @SuppressLint("MissingPermission")
    protected boolean requestBluetoothEnabled(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);

        return bluetoothAdapter.isEnabled();
    }

    public void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
