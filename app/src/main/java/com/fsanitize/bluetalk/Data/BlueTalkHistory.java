package com.fsanitize.bluetalk.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BlueTalkHistory {
    private static final String LOG_TAG = "BlueTalkHistory";
    static HashMap<String, List<BluetoothMessage>> chatHistory = new HashMap<>();
    private static final String CHAT_HISTORY = "chat_history";
    private static final String HISTORY_USERS = "history_users";

    public static <T> List<T> stringToArray(String s, Class<T[]> clazz) {
        T[] arr = new Gson().fromJson(s, clazz);
        return Arrays.asList(arr);
    }

    public void storeHistory(Context context){
        Log.d(LOG_TAG, "ChatHistory:  storing chat histories");
        Gson gson = new Gson();
        SharedPreferences pref = context.getSharedPreferences(HISTORY_USERS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String json;

        //saving users with a chat history
        String[] users = chatHistory.keySet().toArray(new String[0]);
        json = gson.toJson(users);
        editor.putString(HISTORY_USERS, json);
        editor.commit();

        //saving every user chat history
        for(String user : chatHistory.keySet()){
            List<BluetoothMessage> user_history = chatHistory.get(user);
            json = gson.toJson(user_history);
            editor.putString(user,json);
            editor.commit();
        }
    }
    public void restoreHistory(Context context){
        Log.d(LOG_TAG, "ChatHistory:  restoring chat histories");
        Gson gson = new Gson();
        SharedPreferences pref = context.getSharedPreferences(HISTORY_USERS,Context.MODE_PRIVATE);
        String json;
        List<BluetoothMessage> history = new LinkedList<>();

        //getting users with a chat history
        json = pref.getString(HISTORY_USERS,"");
        String[] users = gson.fromJson(json,String[].class);
        if(users == null)
            return;

        //retrieving every user history
        for(String user : users){
            json = pref.getString(user,"");
            history = stringToArray(json, BluetoothMessage[].class);
            chatHistory.put(user,history);
        }
    }
    public void updateUserHistory(String userAddress, List<BluetoothMessage> newChatHistory){
        chatHistory.put(userAddress,newChatHistory);
    }

    public void removeUserHistory(String userAddress){
        chatHistory.remove(userAddress);
    }

    public List<BluetoothMessage> getUserHistory(String userAddress){
        if(chatHistory.containsKey(userAddress))
            return chatHistory.get(userAddress);
        return null;
    }

    public List<UserChatInfo> getUsersWithHistoryInfo(){
        String[] users = chatHistory.keySet().toArray(new String[0]);
        List<UserChatInfo> infos = new ArrayList<>();
        for(String user : users){
            List<BluetoothMessage> user_chat = chatHistory.get(user);
            infos.add(new UserChatInfo(user,user_chat.get(0).getNickname(),user_chat.size()));
        }
        return  infos;
    }

}
