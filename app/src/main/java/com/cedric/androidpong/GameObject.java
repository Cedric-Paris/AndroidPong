package com.cedric.androidpong;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.hardware.SensorEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 07/03/2016.
 */
public abstract class GameObject implements Serializable{

    private transient List<GameObjectEventsObserver> observers = new ArrayList<GameObjectEventsObserver>();

    protected transient Resources appResources;

    protected transient SpriteRenderer sprite;

    protected transient float realXposition;
    protected float posXLeftRelative;
    protected transient float realYposition;
    protected float posYTopRelative;

    public GameObject(Resources resources, int indexSpriteRes, float posXLeftRelative, float posYTopRelative)
    {
        this.posXLeftRelative = posXLeftRelative;
        this.posYTopRelative = posYTopRelative;
        appResources = resources;
        initializeSprite(resources, indexSpriteRes);
    }

    public void setAppResources(Resources resources)
    {
        appResources = resources;
    }

    protected void initializeSprite(Resources resources, int indexSpriteRes)
    {
        sprite = new SpriteRenderer(resources, indexSpriteRes);
    }

    public abstract void updateState(int widthDrawArea, int heightDrawArea, Paddle mainPaddle, float sensorEventValue);

    protected float getRelativePosition(float position, int widthDrawArea)
    {
        return position / widthDrawArea;
    }

    public void drawOnScene(Canvas canvas)
    {
        sprite.draw(canvas, realXposition, realYposition);
    }

    public RectF getCollisionRect()
    {
        return new RectF(realXposition, realYposition, realXposition+sprite.getWidth(), realYposition+sprite.getHeight());
    }

    public float getPosXLeftRelative()
    {
        return posXLeftRelative;
    }

    public float getPosYTopRelative()
    {
        return posYTopRelative;
    }

    public void addListener(GameObjectEventsObserver listener)
    {
        observers.add(listener);
    }

    public void notifyObservers()
    {
        for(GameObjectEventsObserver g : observers)
        {
            g.onGameObjectNeedToBeDestroyed(this);
        }
    }
}
