package com.cedric.androidpong;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 07/03/2016.
 */
public class GameSurfaceView extends SurfaceView implements SensorEventListener, SurfaceHolder.Callback, GameObjectEventsObserver {

    private SurfaceHolder holder;


    private Paddle paddle;
    private List<GameObject> objectsOnScene;
    private List<GameObject> needToBeRemoved;

    private volatile boolean isUpdatingScene;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    public GameSurfaceView(Context context, Resources resources)
    {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        objectsOnScene = new ArrayList<GameObject>();
        needToBeRemoved = new ArrayList<GameObject>();//necessaire pour éviter la modification concurente
        paddle = new Paddle(resources);
        Ball b = new Ball(resources);
        b.addListener(this);
        objectsOnScene.add(b);
        b=new Ball(resources, 100,0,5,-5);
        b.addListener(this);
        objectsOnScene.add(b);
        b=new Ball(resources, 0,100,-5,5);
        b.addListener(this);
        objectsOnScene.add(b);
    }

    public void resume()
    {
        senSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);

    }

    public void pause()
    {
        Log.i("STARTQUITUE DE LA MORT", "Surface : PAUSE first");
        senSensorManager.unregisterListener(this, senAccelerometer);
        Log.i("STARTQUITUE DE LA MORT", "Surface : PAUSE");
    }

    public void drawSurface()
    {
        Canvas canvas = holder.lockCanvas();
        if(canvas == null)
            return;

        canvas.drawARGB(255, 30, 30, 30);
        paddle.drawOnScene(canvas);
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
        Log.i("STARTQUITUE DE LA MORT", "EVENT");

        paddle.updateState(this.getWidth(), this.getHeight(), paddle);
        for(GameObject g : objectsOnScene)
        {
            g.updateState(this.getWidth(), this.getHeight(), paddle);
        }
        objectsOnScene.removeAll(needToBeRemoved);
        needToBeRemoved.clear();
        drawSurface();

        isUpdatingScene = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("STARTQUITUE DE LA MORT", "Surface CREATED");
        paddle.initializePosition(this.getHeight());
        resume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("STARTQUITUE DE LA MORT", "Surface CHANGED");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("STARTQUITUE DE LA MORT", "Surface DESTROYED");
    }

    @Override
    public void onGameObjectNeedToBeDestroyed(GameObject gameObject)
    {
        needToBeRemoved.add(gameObject);
    }
}
