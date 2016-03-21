package com.cedric.androidpong;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.cedric.androidpong.bluetooth.BluetoothGameManager;
import com.cedric.androidpong.game.GameManager;
import com.cedric.androidpong.game.GameSurfaceView;


public class GamePongActivity extends AppCompatActivity {

    private GameSurfaceView gameSurfaceView;
    private GameManager gameManager;

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        gameSurfaceView = new GameSurfaceView(this, this.getResources(), savedInstanceState);
        if(savedInstanceState == null)
        {
            int role = -1;
            try {
                Bundle bundle = getIntent().getExtras();
                role = bundle.getInt("role");
            }
            catch (Exception e)
            {
                finish();
            }
            if(role == -1)
            {
                finish();
            }
            gameManager = new BluetoothGameManager(this, gameSurfaceView, role);
        }
        else
        {
            gameManager = new BluetoothGameManager(this, gameSurfaceView, savedInstanceState);
        }
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
    protected void onResume()
    {
        gameManager.onResume();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        gameManager.onPause();
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        if(gameManager!=null)
            gameManager.onStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        gameManager.saveInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy()
    {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }
}
