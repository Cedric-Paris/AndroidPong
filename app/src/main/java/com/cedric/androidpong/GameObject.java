package com.cedric.androidpong;

import android.content.res.Resources;
import android.graphics.Canvas;

/**
 * Created by Cedric on 07/03/2016.
 */
public abstract class GameObject {

    protected SpriteRenderer sprite;
    protected float posXLeft;
    protected float posYTop;

    public GameObject(Resources resources, int indexSpriteRes, float posXLeft, float posYTop)
    {
        this.posXLeft = posXLeft;
        this.posYTop = posYTop;
        sprite = new SpriteRenderer(resources, indexSpriteRes);
    }

    public abstract void updateState(int widthDrawArea, int heightDrawArea);

    public void drawOnScene(Canvas canvas)
    {
        sprite.draw(canvas, posXLeft, posYTop);
    }


}
