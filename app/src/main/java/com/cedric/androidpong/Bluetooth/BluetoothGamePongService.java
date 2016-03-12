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

    public BluetoothGamePongService(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
        this.context = context;
    }

    public int getState()
    {
        return state;
    }

    public void waitConnection()
    {
        state = STATE_DISCONNECTED;
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
        context.startActivity(discoverableIntent);
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onDeviceFound(device);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                onDiscoveryFinished();
            }
        }
    };

    public void connect(String name)
    {
        state = STATE_DISCONNECTED;
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//Quand on trouve un appareil bluetooth
        context.registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//Quand la découverte est fini
        context.registerReceiver(broadcastReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    private void onDeviceFound(BluetoothDevice bluetoothDevice)
    {
        if(bluetoothDevice.getName() == "LG-E460")
        {
            handler.obtainMessage(BluetoothGameManager.CONNECTING).sendToTarget();
            connectThread = new ConnectThread(bluetoothDevice);
            connectThread.start();
        }
    }

    private void onDiscoveryFinished()
    {
        handler.obtainMessage(12, "FINISHED DISCOVERY").sendToTarget();
    }

    private void connectionFailed()
    {
        state = STATE_DISCONNECTED;
        handler.obtainMessage(BluetoothGameManager.CONNECTION_FAILED).sendToTarget();
    }

    public void connected(BluetoothSocket socket, BluetoothDevice bluetoothDevice)
    {
        state = STATE_CONNECTED;
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




    private class AcceptThread extends Thread {

        private final BluetoothServerSocket listenSocket;
        private volatile boolean isCanceled;

        public AcceptThread()
        {
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
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG, "accept() failed", e);
                    break;
                }
                if (socket != null)
                {
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
        {
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
            cancel();
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
        {
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
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (!isCanceled)
            {
                try
                {
                    bytes = blueInputStream.read(buffer);
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
        }

        public void write(byte[] message)
        {
            try
            {
                blueOutputStream.write(message);
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
            isCanceled = true;
            try
            {
                blueInputStream.close();
                blueInputStream.close();
                bluetoothSocket.close();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "close() of connect socket failed", e);
            }
        }

    }
}
