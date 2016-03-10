package com.cedric.androidpong;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 07/03/2016.
 */
public abstract class GameObject {

    private List<GameObjectEventsObserver> observers = new ArrayList<GameObjectEventsObserver>();

    protected SpriteRenderer sprite;
    protected float posXLeft;
    protected float posYTop;

    public GameObject(Resources resources, int indexSpriteRes, float posXLeft, float posYTop)
    {
        this.posXLeft = posXLeft;
        this.posYTop = posYTop;
        sprite = new SpriteRenderer(resources, indexSpriteRes);
    }

    public abstract void updateState(int widthDrawArea, int heightDrawArea, Paddle mainPaddle);

    public void drawOnScene(Canvas canvas)
    {
        sprite.draw(canvas, posXLeft, posYTop);
    }

    public RectF getCollisionRect()
    {
        return new RectF(posXLeft, posYTop, posXLeft+sprite.getWidth(), posYTop+sprite.getHeight());
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
