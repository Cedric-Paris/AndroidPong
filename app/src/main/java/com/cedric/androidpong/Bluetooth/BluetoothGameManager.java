package com.cedric.androidpong.bluetooth;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cedric.androidpong.R;
import com.cedric.androidpong.game.GameManager;
import com.cedric.androidpong.game.GameSurfaceView;

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

    private AlertDialog simpleMessageBox;
    private ProgressDialog waitingMessageBox;

    private AlertDialog selectDeviceMessageBox;
    private List<String> deviceAvailables = new ArrayList<String>();
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    private BluetoothGamePongService bluetoothService;
    private String deviceAddress;
    private boolean isMessageEnable = true;
    private boolean isReconnecting = false;
    private boolean isPausedOnConnection = false;

    public BluetoothGameManager(AppCompatActivity appCompatActivity, GameSurfaceView gameSurfaceView, int role)
    {
        this.activity = appCompatActivity;
        this.bluetoothRole = role;
        surfaceViewManaged = gameSurfaceView;
        surfaceViewManaged.registerGameManager(this);
        surfaceContext = gameSurfaceView.getContext();
        simpleMessageBox = new AlertDialog.Builder(surfaceContext).create();
        simpleMessageBox.setCancelable(false);
        waitingMessageBox = new ProgressDialog(surfaceContext);
        waitingMessageBox.setCancelable(false);
        waitingMessageBox.setButton(DialogInterface.BUTTON_NEGATIVE, appCompatActivity.getString(R.string.textMessageCancelExit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onStop();
                activity.finish();
            }
        });
    }

    public BluetoothGameManager(AppCompatActivity appCompatActivity, GameSurfaceView gameSurfaceView, Bundle savedInstanceState)
    {
        this(appCompatActivity, gameSurfaceView, savedInstanceState.getInt("BluetoothRole"));
        deviceAddress = savedInstanceState.getString("DeviceAdress");
        this.scoreOther = savedInstanceState.getInt("ScoreOther");
        this.scoreThis = savedInstanceState.getInt("ScoreThis");
    }

    public void start()
    {
        bluetoothService = new BluetoothGamePongService(surfaceContext, handler);
        if(deviceAddress != null)
        {
            onConnectionLost();
            return;
        }
        if(bluetoothRole == CLIENT)
        {
            bluetoothService.getDevices(broadcastReceiver);
            waitingMessageBox.setMessage(activity.getString(R.string.textMessageSearch));
            waitingMessageBox.show();
        }
        else
        {
            bluetoothService.waitConnection(true);
            waitingMessageBox.setMessage(activity.getString(R.string.textMessageWaitConnection));
            waitingMessageBox.show();
        }
    }

    @Override
    public void saveInstanceState(Bundle bundle)
    {
        bundle.putInt("BluetoothRole", bluetoothRole);
        bundle.putInt("ScoreOther", this.scoreOther);
        bundle.putInt("ScoreThis", this.scoreThis);
        if(deviceAddress != null)
        {
            bundle.putString("DeviceAdress", deviceAddress);
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
        builder.setTitle(R.string.textMessageSelectDevice);
        builder.setItems((String[]) deviceAvailables.toArray(new String[1]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bluetoothService.connect(devices.get(which), broadcastReceiver);
            }
        });
        selectDeviceMessageBox = builder.show();
    }

    private void startGame()
    {
        simpleMessageBox.cancel();
        waitingMessageBox.cancel();
        surfaceViewManaged.resume();
        startBallSpawnTask();
    }

    private void onConnectionEstablished()
    {
        isPausedOnConnection = false;
        isReconnecting = false;
        isGameLaunchedAfterConnectionOk = false;
        isCommunicationOkThis = false;
        isCommunicationOkOther = false;
        deviceAddress = bluetoothService.getDeviceConnected().getAddress();
        waitingMessageBox.cancel();
        initializeCommunicationQuery();
    }

    private void onConnectionLost()
    {
        surfaceViewManaged.pause();
        stopBallSpawnTask();
        if(isPausedOnConnection)
        {
            return;
        }
        waitingMessageBox.setMessage(activity.getString(R.string.textMessageConnectionLost));
        waitingMessageBox.show();
        if(bluetoothRole == CLIENT){
            isReconnecting = true;
            reconnectClient();
        }
        else
        {
            bluetoothService.waitConnection(false);
        }
    }

    private void reconnectClient()
    {
        if(deviceAddress == null)
        {
            deviceAddress = bluetoothService.getDeviceConnected().getAddress();
        }
        bluetoothService.connect(deviceAddress);
    }

    private void onConnecting()
    {
        waitingMessageBox.setMessage(activity.getString(R.string.textMessageConnecting));
    }

    private void onConnectionFailed()
    {
        if(bluetoothRole == CLIENT && isReconnecting)
        {
            waitingMessageBox.cancel();
            simpleMessageBox.cancel();
            AlertDialog.Builder builder = new AlertDialog.Builder(surfaceContext);
            builder.setNegativeButton(R.string.textButtonQuit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onStop();
                            activity.finish();
                        }
                    }
            );
            builder.setPositiveButton(R.string.textButtonRetry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    simpleMessageBox.cancel();
                    simpleMessageBox = new AlertDialog.Builder(surfaceContext).create();
                    simpleMessageBox.setCancelable(false);
                    onConnectionLost();
                }
            });
            builder.setMessage(R.string.textMessageConnectionFailed);
            simpleMessageBox = builder.show();
        }

    }

    private void onDiscoveryFinished()
    {
        waitingMessageBox.cancel();
        if(devices.size() > 0)
        {
            return;
        }
        simpleMessageBox.cancel();
        simpleMessageBox.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.textButtonBackMenu), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onStop();
                activity.finish();
            }
        });
        simpleMessageBox.setMessage(activity.getString(R.string.textMessageNoDeviceFound));
        simpleMessageBox.show();

    }

    private final Handler handler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
            if(!isMessageEnable)
            {
                return;
            }
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
                    try {
                        onMessageRead(new String(buffer, 0, msg.arg1));
                    }
                    catch(Exception e) {
                        Log.e("BluetoothGameManager", "Error on message", e);
                    }
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
                bluetoothService.write(CODE_CONNECTION_OK + this.argsSeparator + "NONE");
                break;
            case CODE_CONNECTION_OK:
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
                onBallArrived(Float.parseFloat(tabMessages[2]), Float.parseFloat(tabMessages[3]), Float.parseFloat(tabMessages[4]));
                break;
        }
        if(isCommunicationOkThis && isCommunicationOkOther && !isGameLaunchedAfterConnectionOk)
        {
            isGameLaunchedAfterConnectionOk = true;
            bluetoothService.write(CODE_GAME_LAUNCHED + this.argsSeparator + "NONE");
            startGame();
        }
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

    @Override
    public void onResume()
    {
        if(isPausedOnConnection)
        {
            isPausedOnConnection = false;
            this.start();
        }
    }

    @Override
    public void onPause()
    {
        surfaceViewManaged.pause();
        stopBallSpawnTask();
        if(bluetoothService != null && bluetoothService.getState() == BluetoothGamePongService.STATE_CONNECTED)
        {
            isPausedOnConnection = true;
            this.onStop();
        }
    }

    @Override
    public void onStop()
    {
        if(bluetoothService != null)
        {
            bluetoothService.closeServices();
        }
    }

    @Override
    public void stopManagerOnceAndForAll()
    {
        isMessageEnable = false;
    }
}
