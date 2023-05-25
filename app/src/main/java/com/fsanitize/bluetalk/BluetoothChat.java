package com.fsanitize.bluetalk;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothChat {
    private static final String LOG_TAG = "bluetooth-chat";
    private Handler UIChat_handler = null;
    private BluetoothSocket connectedSocket;
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    private ConnectedThread worker;
    public interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
        public static final int MESSAGE_DISCONNECTION = 3;
    }

    @SuppressLint("MissingPermission")
    public BluetoothChat(BluetoothSocket connectedSocket, Handler UIChat_handler){
        this.UIChat_handler = UIChat_handler;
        this.connectedSocket = connectedSocket;
        this.nickName = connectedSocket.getRemoteDevice().getName();
    }

    @SuppressLint("MissingPermission")
    public BluetoothChat(BluetoothSocket connectedSocket){
        this.connectedSocket = connectedSocket;
        this.nickName = connectedSocket.getRemoteDevice().getName();
    }

    public void attachHandler(Handler UIChat_handler){
        if (UIChat_handler == null) {
            Log.e(LOG_TAG,"Bluetooth Chat: you are attaching a NULL handler");
        }
        this.UIChat_handler = UIChat_handler;

        worker = new ConnectedThread(connectedSocket);
        worker.start();
    }

    public void send(byte[] message){
        worker.write(message);
    }

    public void close(){
        worker.cancel();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket connectedSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket connectedSocket) {
            this.connectedSocket = connectedSocket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = connectedSocket.getInputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = connectedSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes;
            if (UIChat_handler == null) {
                Log.e(LOG_TAG,"Connected thread: your are starting with a NULL handler");
            }

            while (true) {
                try {

                    numBytes = mmInStream.read(mmBuffer);

                    Log.d(LOG_TAG, "Connected Thread: message received : " + mmBuffer);

                    if(UIChat_handler != null) {

                        Message readMsg = UIChat_handler.obtainMessage(
                                MessageConstants.MESSAGE_READ, numBytes, -1,
                                mmBuffer);
                        readMsg.sendToTarget();
                    }
                    else{
                        Log.e(LOG_TAG, "Connected Thread: Handler not attached...result not affecting UI");
                    }
                } catch (IOException e) {
                    sendHandledToast("Chat closed");
                    sendHandledDisconnect();
                    Log.d(LOG_TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                assert (connectedSocket.isConnected());
                mmOutStream.write(bytes);
                Log.d(LOG_TAG, "Connected Thread: message sent : " + bytes.toString());

                if(UIChat_handler != null) {
                    Message writtenMsg = UIChat_handler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, bytes);
                    writtenMsg.sendToTarget();
                }
                else{
                    Log.e(LOG_TAG, "Connected Thread: Handler not attached...result not affecting UI");
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when sending data", e);

                if(UIChat_handler != null) {
                    sendHandledToast("Couldn't send data to the other device");
                }
                else{
                    Log.e(LOG_TAG, "Connected Thread: Handler not attached...result not affecting UI");
                }
            }
        }

        private void sendHandledToast(String message){
            Message writeErrorMsg = UIChat_handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast", message);
            writeErrorMsg.setData(bundle);
            UIChat_handler.sendMessage(writeErrorMsg);
        }

        private void sendHandledDisconnect(){
            Message msg = UIChat_handler.obtainMessage(MessageConstants.MESSAGE_DISCONNECTION);
            UIChat_handler.sendMessage(msg);
        }

        public void cancel() {
            try {
                connectedSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close the connect socket", e);
            }
        }
    }
}
