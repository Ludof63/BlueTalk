package com.fsanitize.bluetalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

public class BluetoothChatActivity extends BluetoothBaseActivity {

    private static final String TAG_LOG = "bluetooth-chat-activity";
    private Toolbar toolbar;
    private EditText editText_reply;
    private Button button_send;
    private RecyclerView recyclerView_chat;
    private LinearLayoutManager linearLayoutManager;
    private Context context;
    private BluetoothMessageListAdapter chat_adapter;
    private List<BluetoothMessage> messageList = new LinkedList<>();


    private Handler UIChat_handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull android.os.Message msg) {
            switch (msg.what){
                case 0:
                    Log.d(TAG_LOG, "Chat Handler: read a message");
                    byte[] buffer = (byte[]) msg.obj;
                    String inputBuffer = new String(buffer, 0, msg.arg1);
                    BluetoothMessage receivedMessage = new BluetoothMessage(inputBuffer,"not me",System.currentTimeMillis());
                    messageList.add(receivedMessage);
                    chat_adapter.notifyDataSetChanged();
                    //Toast.makeText(context, inputBuffer, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Log.d(TAG_LOG, "Chat Handler: wrote a message");
                    byte[] buffer1 = (byte[]) msg.obj;
                    String outputBuffer = new String(buffer1);
                    BluetoothMessage sentMessage = new BluetoothMessage(outputBuffer,MY_ADDRESS,System.currentTimeMillis());
                    messageList.add(sentMessage);
                    chat_adapter.notifyDataSetChanged();
                    linearLayoutManager.scrollToPosition(recyclerView_chat.getAdapter().getItemCount() - 1);
                    Toast.makeText(context,"Message sent",Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    Toast.makeText(context, msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;

                case 3:
                    bluetoothChat.close();
                    finish();
                    break;
            }
            return false;
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_block_user) {
            return true;
        }
        if (id == R.id.action_user_disable_anti_spam) {
            return true;
        }
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Exit Chat");
        builder.setMessage("Do you want to exit? ");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //saveResult();
                bluetoothChat.close();
               BluetoothChatActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                BluetoothChatActivity.super.onBackPressed();
            }
        });
        builder.show();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_chat);

        context = this;

        toolbar = findViewById(R.id.toolbar_chat_activity);
        setSupportActionBar(toolbar);
        //TODO set title to nickname
        getSupportActionBar().setTitle("Nickname");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText_reply = findViewById(R.id.editText_reply);
        button_send = findViewById(R.id.button_send);

        recyclerView_chat = findViewById(R.id.recycler_chat);
        chat_adapter = new BluetoothMessageListAdapter(context,messageList);

        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView_chat.setLayoutManager(linearLayoutManager);
        recyclerView_chat.setAdapter(chat_adapter);

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
            Log.e(TAG_LOG, "Chat Activity: bluetooth chat is null");
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