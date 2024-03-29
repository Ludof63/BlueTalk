package com.fsanitize.bluetalk.UI;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_ADVERTISE;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.fsanitize.bluetalk.Logic.BluetoothChat;
import com.fsanitize.bluetalk.R;
import com.fsanitize.bluetalk.UI.LanguageBaseActivity;

public abstract class BluetoothBaseActivity extends LanguageBaseActivity {
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
    protected static final String CONNECTED_SOCKET = "connected_socket";
    public static final String MY_ADDRESS = "me";
    protected static BluetoothManager bluetoothManager;
    protected static BluetoothAdapter bluetoothAdapter;
    protected static BluetoothChat bluetoothChat = null;

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            showToast(this, getString(R.string.bluetooth_is_not_available_on_this_device));
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
                    showToast(this, getString(R.string.permission_not_granted) + permissions[i]);
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
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
