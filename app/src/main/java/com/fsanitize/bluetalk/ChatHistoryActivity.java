package com.fsanitize.bluetalk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

public class ChatHistoryActivity extends AppCompatActivity {
    private static final String LOG_TAG="chat-history-activity";
    private RecyclerView recyclerView_histories;
    private LinearLayoutManager linearLayoutManager;
    private ChatInfoListAdapter infoListAdapter;
    private List<UserChatInfo> userInfoList = new LinkedList<>();

    private final BlueTalkHistory historyManager = new BlueTalkHistory();



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        ChatHistoryActivity.super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        Toolbar toolbar = findViewById(R.id.toolbar_history_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView_histories = findViewById(R.id.recycler_history);
        infoListAdapter= new ChatInfoListAdapter(this,userInfoList,historyManager);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView_histories.setLayoutManager(linearLayoutManager);
        recyclerView_histories.setAdapter(infoListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        List<UserChatInfo> aux = historyManager.getUsersWithHistoryInfo();
        if(aux.size() != 0)
            Log.d(LOG_TAG, "La history non è vuota");
        else
            Log.d(LOG_TAG, "La history è vuota");
        for(UserChatInfo i : aux){
            Log.d(LOG_TAG, "User con history: " + i.getAddress());
            userInfoList.add(i);
        }
        infoListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        historyManager.storeHistory(this);
    }
}