package com.fsanitize.bluetalk.Logic;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


/**
 * Class that given a BluetoothAdapter returns to an Handler a connectedSocket
 * using the technique of both the two host as client and server
 */
public class BluetoothConnector {

    public interface ConnectorResult {
        public static final int CONNECTION_OK = 0;
        public static final int CONNECTION_TIMEOUT = 1;

        public static final int CONNECTION_RECEIVED = 2;

    }
    private static final UUID BLUETALK_UUID = UUID.fromString("2dbd25f2-f34c-11ed-a05b-0242ac120003");
    public static final int ACK = 42;
    private static final String  SOCKET_NAME = "bluetalkSocket";
    private static final String LOG_TAG = "bluetooth-connector";
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler returnHandler;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;

    public BluetoothConnector(BluetoothAdapter bluetoothAdapter, Handler returnHandler){
        this.bluetoothAdapter = bluetoothAdapter;;
        this.returnHandler = returnHandler;
    }

    public void Listen(){
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void CancelListen(){
        acceptThread.cancel();
    }

    public void Connect(BluetoothDevice device){
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        @SuppressLint("MissingPermission")
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SOCKET_NAME, BLUETALK_UUID);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(LOG_TAG, "Accept Thread: Started");
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    Log.d(LOG_TAG, "Accept Thread: Connection accepted");
                    Message m = new Message();
                    m.what = ConnectorResult.CONNECTION_RECEIVED;
                    m.obj = socket;
                    returnHandler.sendMessage(m);
                    Log.d(LOG_TAG, "Accept Thread: Sent connectedSocket to handler");
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Could not close the connect socket", e);
                    }
                    break;
                }
            }
            Log.d(LOG_TAG, "Accept Thread: Ended");
        }

        public void cancel() {
            Log.d(LOG_TAG, "Accept Thread: Cancel Called");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            this.device = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(BLUETALK_UUID);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Socket's create() method failed", e);
            }
            socket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            Log.d(LOG_TAG, "Connect Thread: Started");
            if(bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
            } catch (IOException connectException) {
                    Message m = new Message();
                    m.what = ConnectorResult.CONNECTION_TIMEOUT;
                    m.obj = device.getName();
                    returnHandler.sendMessage(m);
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(LOG_TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            //WAITING FOR ACK
            try {
                InputStream inputStream = socket.getInputStream();
                Log.d(LOG_TAG, "Connect Thread: Waiting for ACK");
                if(ACK == inputStream.read()){
                    Log.d(LOG_TAG, "Connect Thread: RECEIVED ACK: connection succeeded -> cancelling listen");
                    acceptThread.cancel();
                    Message m = new Message();
                    m.what = ConnectorResult.CONNECTION_OK;
                    m.obj = socket;
                    returnHandler.sendMessage(m);
                    Log.d(LOG_TAG, "Connect Thread: connectedSocket sent to handler");
                    return;
                }
                else{
                    Log.e(LOG_TAG, "Connect Thread: first message received was not ACK");
                }

            } catch (IOException e) {
                Message m = new Message();
                m.what = ConnectorResult.CONNECTION_TIMEOUT;
                m.obj = device.getName();
                returnHandler.sendMessage(m);
            }

            Log.d(LOG_TAG, "Connect Thread: Ended");
        }


        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close the client socket", e);
            }
        }
    }
}
