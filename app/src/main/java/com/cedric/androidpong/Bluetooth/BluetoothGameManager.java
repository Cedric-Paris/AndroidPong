package com.cedric.androidpong.Bluetooth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.cedric.androidpong.Ball;
import com.cedric.androidpong.GameManager;
import com.cedric.androidpong.GameSurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cedric on 12/03/2016.
 */
public class BluetoothGameManager extends GameManager
{
    public static final int CONNECTION_ESTABLISHED = 1;
    public static final int CONNECTION_LOST = 2;
    public static final int CONNECTING = 3;
    public static final int CONNECTION_FAILED = 4;
    public static final int DISCOVERY_FINISHED =5;
    public static final int MESSAGE_READ = 10;

    public static final int CLIENT = 1;
    public static final int SERVER = 2;
    public int bluetoothRole = 0;

    private Context surfaceContext;
    private AlertDialog simpleMessageBox;
    private ProgressDialog waitingMessageBox;

    private AlertDialog selectDeviceMessageBox;
    private List<String> deviceAvailables = new ArrayList<String>();
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    private BluetoothGamePongService bluetoothService;

    public BluetoothGameManager(GameSurfaceView gameSurfaceView, int role, Bundle savedInstanceState)
    {
        if(savedInstanceState != null) {
            this.bluetoothRole = savedInstanceState.getInt("BluetoothRole");
        }
        else{
            this.bluetoothRole = role;
        }
        surfaceViewManaged = gameSurfaceView;
        surfaceViewManaged.registerGameManager(this);
        surfaceContext = gameSurfaceView.getContext();
        simpleMessageBox = new AlertDialog.Builder(surfaceContext).create();
        simpleMessageBox.setCancelable(false);
        waitingMessageBox = new ProgressDialog(surfaceContext);
        waitingMessageBox.setCancelable(false);
    }

    public void start()
    {
        bluetoothService = new BluetoothGamePongService(surfaceContext, handler);
        if(bluetoothRole == CLIENT)
        {
            bluetoothService.getDevices(broadcastReceiver);
            waitingMessageBox.setMessage("Search...");
            waitingMessageBox.show();
        }
        else
        {
            bluetoothService.waitConnection();
            waitingMessageBox.setMessage("Waiting for connection . . .");
            waitingMessageBox.show();
        }
    }

    @Override
    public void saveInstanceState(Bundle bundle)
    {
        bundle.putInt("BluetoothRole", bluetoothRole);
        if(bluetoothService != null && bluetoothService.getState() == BluetoothGamePongService.STATE_CONNECTED)
        {
            bundle.putString("DeviceAdress", bluetoothService.getDeviceConnected().getAddress());
        }
        surfaceViewManaged.saveInstanceState(bundle);
    }

    @Override
    public void notifyOtherBallLost()
    {
        bluetoothService.write(CODE_BALL_LOST + this.argsSeparator + "BALL LOST");
    }

    @Override
    public void sendBallToOther(float ballXPosition, float xVectorDirection, float yVectorDirection)
    {
        bluetoothService.write(CODE_BALL_ARRIVED + this.argsSeparator + "BALL ARRIVED" + this.argsSeparator + ballXPosition + this.argsSeparator + xVectorDirection + this.argsSeparator+yVectorDirection);
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

    private void onDeviceFound(BluetoothDevice bluetoothDevice)
    {
        waitingMessageBox.cancel();
        devices.add(bluetoothDevice);
        deviceAvailables.add(bluetoothDevice.getName());
        if(selectDeviceMessageBox != null)
        {
            selectDeviceMessageBox.cancel();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this.surfaceContext);
        builder.setTitle("Select a device");
        builder.setItems((String[])deviceAvailables.toArray(new String[1]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bluetoothService.connect(devices.get(which), broadcastReceiver);
            }
        });
        selectDeviceMessageBox = builder.show();
    }

    private void startGame()
    {
        surfaceViewManaged.resume();
    }

    private void onConnectionEstablished()
    {
        waitingMessageBox.cancel();
        initializeCommunicationQuery();
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
        waitingMessageBox.cancel();
        if(devices.size() > 0)
        {
            return;
        }
        simpleMessageBox.setMessage("No device Found");
        simpleMessageBox.show();

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
                case MESSAGE_READ:
                    byte[] buffer = (byte[])msg.obj;
                    onMessageRead(new String(buffer, 0, msg.arg1));
                    break;
            }
        }
    };


    private static final String argsSeparator = "!";

    private static final int CODE_CONNECTION_QUERY = 1;
    private static final int CODE_CONNECTION_OK = 2;
    private static final int CODE_GAME_LAUNCHED = 3;
    private static final int CODE_BALL_LOST = 4;
    private static final int CODE_BALL_ARRIVED = 5;


    private boolean isCommunicationOkThis;
    private boolean isCommunicationOkOther;
    private boolean isGameLaunchedAfterConnectionOk;

    private synchronized void onMessageRead(String message)
    {
        String[] tabMessages = message.split(argsSeparator);

        int msgCode = Integer.parseInt(tabMessages[0]);
        switch(msgCode)
        {
            case CODE_CONNECTION_QUERY:
                isCommunicationOkOther = true;
                Log.i("SSSSSSSSSSSSSSSS","Other Ok");
                bluetoothService.write(CODE_CONNECTION_OK + this.argsSeparator + "NONE");
                break;
            case CODE_CONNECTION_OK:
                Log.i("SSSSSSSSSSSSSSSS","This OK");
                isCommunicationOkThis = true;
                break;
            case CODE_GAME_LAUNCHED:
                isGameLaunchedAfterConnectionOk = true;
                startGame();
                break;
            case CODE_BALL_LOST:
                onOtherLostBall();
                break;
            case CODE_BALL_ARRIVED:
                onBallArrived(Float.parseFloat(tabMessages[2]),Float.parseFloat(tabMessages[3]), Float.parseFloat(tabMessages[4]));
                break;
        }
        if(isCommunicationOkThis && isCommunicationOkOther && !isGameLaunchedAfterConnectionOk)
        {
            isGameLaunchedAfterConnectionOk = true;
            bluetoothService.write(CODE_GAME_LAUNCHED + this.argsSeparator + "NONE");
            startGame();
        }
        Log.i("TESTESTESTEST0", message);
    }

    private TimerTask communicationQuery;

    private void initializeCommunicationQuery()
    {
        long delaisAvantDepart = 0;
        long tempsEntreDeuxTaches = 2000;//1000 => 1s
        communicationQuery = new TimerTask(){

            @Override
            public void run() {
                bluetoothService.write(CODE_CONNECTION_QUERY + argsSeparator + "NONE");
                if(isCommunicationOkThis)
                    cancel();
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(communicationQuery, delaisAvantDepart, tempsEntreDeuxTaches);
    }

    public void onDestroy()
    {
        if(bluetoothService != null)
        {
            bluetoothService.closeServices();
        }
    }
}
