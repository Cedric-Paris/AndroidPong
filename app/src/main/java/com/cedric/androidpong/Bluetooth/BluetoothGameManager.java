package com.cedric.androidpong.Bluetooth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.cedric.androidpong.GameSurfaceView;

/**
 * Created by Cedric on 12/03/2016.
 */
public class BluetoothGameManager
{
    public static final int CONNECTION_ESTABLISHED = 1;
    public static final int CONNECTION_LOST = 2;
    public static final int CONNECTING = 3;
    public static final int CONNECTION_FAILED = 4;
    public static final int DISCOVERY_FINISHED =5;
    public static final int MESSAGE_READ = 10;

    private GameSurfaceView surfaceViewManaged;
    private Context surfaceContext;
    private AlertDialog simpleMessageBox;
    private ProgressDialog waitingMessageBox;

    private BluetoothGamePongService bluetoothService;

    public BluetoothGameManager(GameSurfaceView gameSurfaceView)
    {
        surfaceViewManaged = gameSurfaceView;
        surfaceContext = gameSurfaceView.getContext();
        simpleMessageBox = new AlertDialog.Builder(surfaceContext).create();
        simpleMessageBox.setCancelable(false);
        waitingMessageBox = new ProgressDialog(surfaceContext);
        waitingMessageBox.setCancelable(false);
    }

    public void start()
    {
        bluetoothService = new BluetoothGamePongService(surfaceContext, handler);
        bluetoothService.waitConnection();
        bluetoothService.connect("Device Name");
        waitingMessageBox.setMessage("Waiting for connection . . .");
        waitingMessageBox.show();
    }

    private void onConnectionEstablished()
    {
        waitingMessageBox.cancel();
        surfaceViewManaged.resume();
    }

    private void onConnectionLost()
    {
        surfaceViewManaged.pause();
        waitingMessageBox.setMessage("Connection lost :(\nReconnecting . . .");
        waitingMessageBox.show();
    }

    private void onConnecting()
    {
        waitingMessageBox.setMessage("Connecting . . .");
    }

    private void onConnectionFailed()
    {
        waitingMessageBox.setMessage("Connection failed :(\nWaiting for connection . . .");
    }

    private void onDiscoveryFinished()
    {
        //if(bluetoothService.getState() == bluetoothService.STATE_CONNECTED)

    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case CONNECTION_ESTABLISHED:
                    onConnectionEstablished();
                    break;
                case CONNECTING:
                    onConnecting();
                    break;
                case CONNECTION_FAILED :
                    onConnectionFailed();
                    break;
                case CONNECTION_LOST:
                    onConnectionLost();
                    break;
                case DISCOVERY_FINISHED:
                    onDiscoveryFinished();
                    break;
            }
        }
    };
}
