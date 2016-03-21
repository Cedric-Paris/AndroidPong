package com.cedric.androidpong.Bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Cedric on 12/03/2016.
 */
public class BluetoothGamePongService {

    private static final String LOG_TAG = "BluetoothGamePongServic";

    private static final String NAME = "AndroidGamePong";
    private static final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0845200c9a66");

    private BluetoothAdapter bluetoothAdapter;

    private Context context;
    private Handler handler;

    private volatile int state = STATE_DISCONNECTED;
    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_CONNECTED = 2;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private BluetoothDevice deviceConnected;


    public BluetoothGamePongService(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
        this.context = context;
    }

    public BluetoothDevice getDeviceConnected()
    {
        return deviceConnected;
    }

    public int getState()
    {
        return state;
    }

    public void waitConnection(boolean needDiscoverable)
    {
        state = STATE_DISCONNECTED;
        if(needDiscoverable)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
            context.startActivity(discoverableIntent);
        }
        acceptThread = new AcceptThread();
        acceptThread.start();
    }



    public void getDevices(BroadcastReceiver broadcastReceiver)
    {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//Quand on trouve un appareil bluetooth
        context.registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//Quand la découverte est fini
        context.registerReceiver(broadcastReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    public void connect(String deviceAddress)
    {
        state = STATE_DISCONNECTED;
        connectThread = new ConnectThread(bluetoothAdapter.getRemoteDevice(deviceAddress));
        connectThread.start();
    }

    public void connect(BluetoothDevice device, BroadcastReceiver broadcastReceiver)
    {
        state = STATE_DISCONNECTED;
        connectThread = new ConnectThread(device);
        connectThread.start();
        context.unregisterReceiver(broadcastReceiver);
        onDiscoveryFinished();

    }

    private void onDiscoveryFinished()
    {
        handler.obtainMessage(BluetoothGameManager.DISCOVERY_FINISHED, "FINISHED DISCOVERY").sendToTarget();
    }

    private void connectionFailed()
    {
        state = STATE_DISCONNECTED;
        handler.obtainMessage(BluetoothGameManager.CONNECTION_FAILED).sendToTarget();
    }

    public void connected(BluetoothSocket socket, BluetoothDevice bluetoothDevice)
    {
        state = STATE_CONNECTED;
        deviceConnected = bluetoothDevice;
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        handler.obtainMessage(BluetoothGameManager.CONNECTION_ESTABLISHED).sendToTarget();
    }

    private void connectionLost()
    {
        state = STATE_DISCONNECTED;
        handler.obtainMessage(BluetoothGameManager.CONNECTION_LOST).sendToTarget();
        connectedThread = null;
    }

    public void write(String message)
    {
        if(connectedThread == null)
            return;
        connectedThread.write(message.getBytes());
    }

    public void closeServices()
    {
        state = STATE_DISCONNECTED;
        if(acceptThread != null)
            acceptThread.cancel();
        if(connectThread != null)
            connectThread.cancel();
        if(connectedThread != null)
            connectedThread.cancel();
    }


    private class AcceptThread extends Thread {

        private final BluetoothServerSocket listenSocket;
        private volatile boolean isCanceled;

        public AcceptThread()
        {
            Log.e(LOG_TAG, "Start Accept Thread");
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, APP_UUID);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Start listen failed" + e.getMessage());
            }
            listenSocket = tmp;
            //GERER LE CAS NULL
        }

        public void run() {
            setName("Accept Connection Thread");
            BluetoothSocket socket = null;

            while (state != STATE_CONNECTED && !isCanceled)
            {
                try
                {
                    socket = listenSocket.accept();//Retourne que si connection ou exception
                    Log.i(LOG_TAG, "Socket Accepter");
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG, "accept() failed", e);
                    break;
                }
                if (socket != null)
                {
                    Log.i(LOG_TAG, "socket not null");
                    connected(socket, socket.getRemoteDevice());
                    break;
                }
            }
            cancel();
            acceptThread = null;
        }

        public void cancel()
        {
            isCanceled =true;

            try
            {
                Log.i("BluetoothGamePongServic" , "Close listen socket");
                listenSocket.close();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "close() of server failed", e);
            }
        }
    }




    private class ConnectThread extends Thread
    {
        private final BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice device)
        {Log.e(LOG_TAG, "Start Connect Thread");
            BluetoothSocket tmp = null;
            try
            {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "create() failed", e);
            }
            bluetoothSocket = tmp;
        }

        public void run()
        {
            setName("Try Connect Thread");
            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();
            try
            {
                bluetoothSocket.connect();//Retourne que si connection ou exception
            }
            catch (IOException e)
            {
                connectionFailed();
                cancel();
                return;
            }
            connectThread = null;
            connected(bluetoothSocket, bluetoothSocket.getRemoteDevice());
        }

        public void cancel()
        {
            try {
                bluetoothSocket.close();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "close() of connect socket failed", e);
            }
        }
    }




    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream blueInputStream;
        private final OutputStream blueOutputStream;

        private volatile boolean isCanceled;

        public ConnectedThread(BluetoothSocket socket)
        {Log.e(LOG_TAG, "Start Connected Thread");
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Temp sockets not created", e);
            }
            blueInputStream = tmpIn;
            blueOutputStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[500];
            int bytes;
            // Keep listening to the InputStream while connected
            while (!isCanceled)
            {
                try
                {
                    bytes = blueInputStream.read(buffer, 0, buffer.length);
                    Log.i("BluetoothGamePongServic", "READ STRING"+ new String(buffer, 0, bytes));
                    handler.obtainMessage(BluetoothGameManager.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG, "disconnected", e);
                    cancel();
                    connectionLost();
                    break;
                }
            }
            cancel();
        }

        public void write(byte[] message)
        {
            Log.i("BluetoothGamePongServic","Write message"+new String(message));
            try
            {
                blueOutputStream.write(message);
                blueOutputStream.flush();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Exception during write", e);
                cancel();
                connectionLost();
            }
        }

        public void cancel()
        {
            Log.i("BluetoothGamePongServic", "Cancel connected thread");
            isCanceled = true;
            try
            {
                blueInputStream.close();
                blueOutputStream.close();
                bluetoothSocket.close();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "close() of connect socket failed", e);
            }

        }

    }
}
