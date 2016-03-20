package com.cedric.androidpong;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.cedric.androidpong.Bluetooth.BluetoothGameManager;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Button createGameButton = (Button) findViewById(R.id.createGameButton);
        Button joinGameButton = (Button) findViewById(R.id.joinGameButton);
        Button quitAppButton = (Button) findViewById(R.id.quitAppButton);

        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("role", BluetoothGameManager.SERVER);

                Intent intent = new Intent(MenuActivity.this, GamePongActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("role", BluetoothGameManager.CLIENT);

                Intent intent = new Intent(MenuActivity.this, GamePongActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        quitAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }
}
