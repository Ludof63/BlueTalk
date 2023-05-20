package com.fsanitize.bluetalk;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

public class BluetoothChatActivity extends BluetoothBaseActivity {

    private static final String TAG_LOG = "bluetooth-chat-activity";
    private Toolbar toolbar;
    private EditText editText_reply;
    private Button button_send;
    private Context context;


    private Handler UIChat_handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0:
                    byte[] buffer = (byte[]) msg.obj;
                    String inputBuffer = new String(buffer, 0, msg.arg1);
                    Toast.makeText(context, inputBuffer, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    byte[] buffer1 = (byte[]) msg.obj;
                    String outputBuffer = new String(buffer1);
                    Toast.makeText(context,"Message sent",Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    Toast.makeText(context, msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_chat);

        context = this;

        toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);

        editText_reply = findViewById(R.id.editText_reply);
        button_send = findViewById(R.id.button_send);

        button_send.setOnClickListener(view -> {
            String message = editText_reply.getText().toString();
            if (!message.isEmpty()) {
                editText_reply.setText("");
                bluetoothChat.send(message.getBytes());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        BluetoothSocket connectedSocket = null;
        if(bluetoothChat == null){
            Log.e(TAG_LOG, "bluetooth chat is null");
            finish();
        }
        bluetoothChat.attachHandler(UIChat_handler);
    }

    @Override
    protected void handlerBluetoothIsEnabled() {

    }

    @Override
    protected void handlerBluetoothIsDisabled() {

    }
}