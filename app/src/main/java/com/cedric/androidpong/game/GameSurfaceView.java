package com.cedric.androidpong.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.cedric.androidpong.bluetooth.BluetoothGameManager;
import com.cedric.androidpong.gameobject.Paddle;
import com.cedric.androidpong.gameobject.Ball;
import com.cedric.androidpong.gameobject.GameObject;
import com.cedric.androidpong.gameobject.GameObjectEventsObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Cedric on 07/03/2016.
 */
public class GameSurfaceView extends SurfaceView implements SensorEventListener, SurfaceHolder.Callback, GameObjectEventsObserver {

    private SurfaceHolder holder;

    private Paint paintTextStyle;
    private static final int COLOR_THIS = Color.argb(150, 0, 162, 232);
    private static final int COLOR_OTHER = Color.argb(150, 255, 127, 39);

    private Paddle paddle;
    private List<GameObject> objectsOnScene;
    private volatile List<GameObject> needToBeRemoved;
    private volatile List<GameObject> needToBeAdded;

    private volatile boolean isUpdatingScene;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private int orientationNumber = 0;//0 = portrait 1= paysage
    private int orientationEffectOnValues = -1;

    private GameManager gameManager;

    public GameSurfaceView(Context context, Resources resources, Bundle savedInstanceState)
    {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        objectsOnScene = new ArrayList<GameObject>();
        needToBeRemoved = new ArrayList<GameObject>();//necessaire pour éviter la modification concurente
        needToBeAdded = new ArrayList<GameObject>();

        paintTextStyle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextStyle.setStyle(Paint.Style.FILL);

        if(savedInstanceState != null)
        {
            restoreInstanceState(resources, savedInstanceState);
            return;
        }
        paddle = new Paddle(resources);
        Random rand = new Random();
        rand.nextFloat();
        Ball ball = new Ball(resources, rand.nextFloat(),0,rand.nextFloat(),1.0f);
        ball.addListener(this);
        objectsOnScene.add(ball);
    }

    public void registerGameManager(BluetoothGameManager manager)
    {
        this.gameManager = manager;
    }

    private void restoreInstanceState(Resources resources, Bundle savedInstanceState)
    {
        paddle = (Paddle)savedInstanceState.getSerializable("Paddle");
        String[] gameObjectsKeys = savedInstanceState.getStringArray("GameObjectKeys");
        GameObject gameObject;
        for(String key : gameObjectsKeys )
        {
            gameObject = (GameObject)savedInstanceState.getSerializable(key);
            gameObject.addListener(this);
            gameObject.setAppResources(resources);
            objectsOnScene.add(gameObject);
        }
    }

    public void resume()
    {
        senSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);

    }

    public void pause()
    {
        if(senSensorManager != null)
        {
            senSensorManager.unregisterListener(this, senAccelerometer);
            senSensorManager = null;
        }
    }

    public void drawSurface()
    {
        Canvas canvas = holder.lockCanvas();
        if(canvas == null)
            return;

        canvas.drawARGB(255, 30, 30, 30);
        paddle.drawOnScene(canvas);
        paintTextStyle.setTextAlign(Paint.Align.RIGHT);
        paintTextStyle.setColor(COLOR_THIS);
        canvas.drawText(gameManager.getScoreThis() + " ", this.getWidth() / 2, (this.getHeight() * 0.07f) + paintTextStyle.getTextSize(), paintTextStyle);
        paintTextStyle.setTextAlign(Paint.Align.LEFT);
        paintTextStyle.setColor(COLOR_OTHER);
        canvas.drawText(" "+gameManager.getScoreOther(),this.getWidth()/2, (this.getHeight()*0.07f)+paintTextStyle.getTextSize(), paintTextStyle);
        for(GameObject g : objectsOnScene)
            g.drawOnScene(canvas);

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if(isUpdatingScene)
            return;
        isUpdatingScene = true;

        paddle.updateState(this.getWidth(), this.getHeight(), paddle, event.values[orientationNumber]*orientationEffectOnValues);
        synchronized (needToBeAdded)
        {
            objectsOnScene.addAll(needToBeAdded);
            needToBeAdded.clear();
        }
        for(GameObject g : objectsOnScene)
        {
            g.updateState(this.getWidth(), this.getHeight(), paddle, event.values[orientationNumber] * orientationEffectOnValues);
        }
        synchronized (needToBeRemoved)
        {
            objectsOnScene.removeAll(needToBeRemoved);
            needToBeRemoved.clear();
        }
        drawSurface();

        isUpdatingScene = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        paddle.initializePosition(this.getHeight());
        paintTextStyle.setTextSize((this.getWidth() * 0.2f));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        switch(this.getDeviceOrientation())
        {
            case Surface.ROTATION_0:
                orientationNumber = 0;
                orientationEffectOnValues= -1;
                break;
            case Surface.ROTATION_90:
                orientationNumber = 1;
                orientationEffectOnValues = 1;
                break;
            case Surface.ROTATION_270:
                orientationNumber = 1;
                orientationEffectOnValues = -1;
                break;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    private int getDeviceOrientation()
    {
        return ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
    }

    public void saveInstanceState(Bundle bundle)
    {
        String[] gameObjectsKeys = new String[objectsOnScene.size()];
        int i =0;
        for(GameObject gameObject : objectsOnScene)
        {
            gameObjectsKeys[i] = "GameObjectKeySave"+i;
            bundle.putSerializable(gameObjectsKeys[i], gameObject);
            i++;
        }
        bundle.putStringArray("GameObjectKeys", gameObjectsKeys);
        bundle.putSerializable("Paddle", paddle);
    }

    public void addObjectOnScene(GameObject gameObject)
    {
        gameObject.addListener(this);
        synchronized (needToBeAdded)
        {
            needToBeAdded.add(gameObject);
        }
    }

    @Override
    public void onGameObjectNeedToBeDestroyed(GameObject gameObject)
    {
        synchronized (needToBeRemoved)
        {
            needToBeRemoved.add(gameObject);
            if(gameObject.getClass().getName() == Ball.class.getName())
            {
                gameManager.onBallDestroyed((Ball)gameObject);
            }
        }
    }

}
