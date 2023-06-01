package com.fsanitize.bluetalk.UI;

import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fsanitize.bluetalk.Data.BlueTalkHistory;
import com.fsanitize.bluetalk.R;

public class MainActivity extends BluetoothBaseActivity {

    private TextView text_error;
    private Button button_start;
    private Button button_discover;
    private final BlueTalkHistory chat_history = new BlueTalkHistory();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_language) {
            String[] LANG = {getString(R.string.english_language), getString(R.string.italian_language)};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.select_a_language);
            builder.setItems(LANG, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0:
                            //en
                            changeLanguage("en");
                            saveLanguagePreference("en");
                            recreate();
                            break;
                        case 1:
                            //it
                            changeLanguage("it");
                            saveLanguagePreference("it");
                            recreate();
                            break;
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return true;
        }
        if (id == R.id.action_chat_history) {
            chat_history.restoreHistory(this);
            startActivity(new Intent(this, ChatHistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

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
            chat_history.restoreHistory(this);
            startActivity(new Intent(this, DeviceListActivity.class));
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