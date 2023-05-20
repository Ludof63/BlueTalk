package com.fsanitize.bluetalk;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends BluetoothBaseActivity {

    TextView text_error;
    Button button_start;
    Button button_discover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        text_error = findViewById(R.id.text_error_main);
        button_start = findViewById(R.id.button_start_main);
        button_discover = findViewById(R.id.button_discover_main);
        
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(bluetoothAdapter.isEnabled())
            handlerBluetoothIsEnabled();
        else
            handlerBluetoothIsDisabled();

        button_start.setOnClickListener(view ->{
            if(requestBluetoothEnabled())
                handlerBluetoothIsEnabled();
            else
                handlerBluetoothIsDisabled();
        });
        button_discover.setOnClickListener(view -> {
            startActivity(new Intent(this,DeviceListActivity.class));
        });
    }

    @Override
    protected void handlerBluetoothIsEnabled() {
        makeStarter(true);
    }

    @Override
    protected void handlerBluetoothIsDisabled() {
        makeStarter(false);
    }

    private void makeStarter(boolean isBluetoothEnabled){
        button_start.setEnabled(!isBluetoothEnabled);
        text_error.setVisibility(isBluetoothEnabled?View.INVISIBLE:View.VISIBLE);
        button_start.setVisibility(isBluetoothEnabled?View.INVISIBLE:View.VISIBLE);

        button_discover.setEnabled(isBluetoothEnabled);
        button_discover.setVisibility(isBluetoothEnabled?View.VISIBLE:View.INVISIBLE);
    }
    
}