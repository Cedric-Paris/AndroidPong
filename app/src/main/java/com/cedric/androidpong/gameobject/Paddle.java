package com.cedric.androidpong.gameobject;

import android.content.res.Resources;

import com.cedric.androidpong.R;

/**
 * Created by Cedric on 07/03/2016.
 */
public class Paddle extends GameObject {

    private transient float xSpeed = 0;

    public Paddle(Resources resources, float posXLeftRelative, float posYTop)
    {
        super(resources, R.drawable.paddle_sprite, posXLeftRelative, posYTop);
    }

    public Paddle(Resources resources)
    {
        super(resources, R.drawable.paddle_sprite, 50, 50);
    }

    public void updateState(int widthDrawArea, int heightDrawArea, Paddle mainPaddle, float sensorEventValue)
    {
        if(sprite == null)
            initializeSprite(appResources , R.drawable.paddle_sprite);
        xSpeed = sensorEventValue*(widthDrawArea*0.01f);
        realXposition += xSpeed;
        if(realXposition > (widthDrawArea-sprite.getWidth()))
            realXposition = widthDrawArea-sprite.getWidth();
        if(realXposition < 0)
            realXposition = 0;

        posXLeftRelative = getRelativePosition(realXposition, widthDrawArea);
    }

    public void initializePosition(int surfaceHeigth)
    {
        int height = sprite.getHeight();
        realYposition = surfaceHeigth - height - surfaceHeigth*0.05f;
        posYTopRelative = getRelativePosition(realYposition, surfaceHeigth);
    }
}
