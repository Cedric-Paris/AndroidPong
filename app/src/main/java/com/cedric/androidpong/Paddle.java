package com.cedric.androidpong;

import android.content.res.Resources;
import android.hardware.SensorEvent;
import android.util.Log;

/**
 * Created by Cedric on 07/03/2016.
 */
public class Paddle extends GameObject{

    private float xSpeed = 0;

    public Paddle(Resources resources, float posXLeft, float posYTop)
    {
        super(resources, R.drawable.paddle_sprite, posXLeft, posYTop);
    }

    public Paddle(Resources resources)
    {
        super(resources, R.drawable.paddle_sprite, 50, 50);
    }

    public void updateState(int widthDrawArea, int heightDrawArea, Paddle mainPaddle, float sensorEventValue)
    {
        xSpeed = sensorEventValue*(widthDrawArea*0.01f);
        posXLeft += xSpeed;
        if(posXLeft > (widthDrawArea-sprite.getWidth()))
            posXLeft = widthDrawArea-sprite.getWidth();
        if(posXLeft < 0)
            posXLeft = 0;
    }

    public void initializePosition(int surfaceHeigth)
    {
        int height = sprite.getHeight();
        posYTop = surfaceHeigth - height - surfaceHeigth*0.05f;

    }
}
