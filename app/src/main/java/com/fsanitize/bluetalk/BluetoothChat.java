package com.fsanitize.bluetalk;

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
    private ConnectedThread worker;
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public BluetoothChat(BluetoothSocket connectedSocket, Handler UIChat_handler){
        this.UIChat_handler = UIChat_handler;
        this.connectedSocket = connectedSocket;

        worker = new ConnectedThread(connectedSocket);
        worker.start();
    }

    public BluetoothChat(BluetoothSocket connectedSocket){
        this.connectedSocket = connectedSocket;

        worker = new ConnectedThread(connectedSocket);
        worker.start();
    }

    public void attachHandler(Handler UIChat_handler){
        this.UIChat_handler = UIChat_handler;
    }

    public void send(byte[] message){
        worker.write(message);
    }

    public void close(){
        worker.cancel();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes;

            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
                    Log.d(LOG_TAG, "Connected Thread: message received : " + mmBuffer);

                    if(UIChat_handler != null) {
                        Log.e(LOG_TAG, "Connected Thread: Handler not attached...result not affecting UI");
                        Message readMsg = UIChat_handler.obtainMessage(
                                MessageConstants.MESSAGE_READ, numBytes, -1,
                                mmBuffer);
                        readMsg.sendToTarget();
                    }
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {

                mmOutStream.write(bytes);
                Log.d(LOG_TAG, "Connected Thread: message sent : " + bytes.toString());

                if(UIChat_handler != null) {
                    Log.e(LOG_TAG, "Connected Thread: Handler not attached...result not affecting UI");
                    Message writtenMsg = UIChat_handler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                    writtenMsg.sendToTarget();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when sending data", e);

                if(UIChat_handler != null) {
                    Log.e(LOG_TAG, "Connected Thread: Handler not attached...result not affecting UI");
                    sendHandledToast("Couldn't send data to the other device");
                }
            }
        }

        private void sendHandledToast(String message){
            Message writeErrorMsg =
                    UIChat_handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast", message);
            writeErrorMsg.setData(bundle);
            UIChat_handler.sendMessage(writeErrorMsg);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close the connect socket", e);
            }
        }
    }
}
