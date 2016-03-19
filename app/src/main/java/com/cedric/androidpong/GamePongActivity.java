package com.cedric.androidpong;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.cedric.androidpong.Bluetooth.BluetoothGameManager;

import java.util.jar.Manifest;


public class GamePongActivity extends AppCompatActivity {

    private GameSurfaceView gameSurfaceView;
    private BluetoothGameManager gameManager;

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        gameSurfaceView = new GameSurfaceView(this, this.getResources(), savedInstanceState);
        gameManager = new BluetoothGameManager(gameSurfaceView, BluetoothGameManager.SERVER, savedInstanceState);
        //gameSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//Supprime la barre de l'heure en haut
        setContentView(gameSurfaceView);
        initializeBluetooth();
    }

    private void initializeBluetooth()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK)
            {
                gameManager.start();
            }
            else
            {
                Toast.makeText(this, R.string.bluetooth_unable, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    @Override
    public void onStart()
    {
        Log.i("BluetoothGame","Start GameManager");
        super.onStart();
        if (!bluetoothAdapter.isEnabled())
        {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        }
        else
        {
            gameManager.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause(){
        Log.i("STARTQUITUE DE LA MORT", "APP : PAUSE");
        gameSurfaceView.pause();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        gameManager.saveInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Log.i("STARTQUITUE DE LA MORT", "DESTROY");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(gameManager!=null)
            gameManager.onDestroy();
        super.onDestroy();
    }
}
