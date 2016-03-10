package com.cedric.androidpong;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;


public class GamePongActivity extends AppCompatActivity {

    private GameSurfaceView gameSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        gameSurfaceView = new GameSurfaceView(this, this.getResources());
        //gameSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//Supprime la barre de l'heure en haut
        setContentView(gameSurfaceView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //gameSurfaceView.resume();
    }

    @Override
    protected void onPause(){
        Log.i("STARTQUITUE DE LA MORT", "APP : PAUSE");
        gameSurfaceView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i("STARTQUITUE DE LA MORT", "DESTROY");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }
}
