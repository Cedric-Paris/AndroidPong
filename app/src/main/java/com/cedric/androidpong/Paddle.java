package com.cedric.androidpong;

import android.content.res.Resources;
import android.util.Log;

/**
 * Created by Cedric on 07/03/2016.
 */
public class Paddle extends GameObject{

    public Paddle(Resources resources, float posXLeft, float posYTop)
    {
        super(resources, R.drawable.paddle_sprite, posXLeft, posYTop);
    }

    public Paddle(Resources resources)
    {
        super(resources, R.drawable.paddle_sprite, 50, 50);
    }

    int v = 3;//temp
    public void updateState(int widthDrawArea, int heightDrawArea, Paddle mainPaddle)
    {
        //posXLeft+=1;
        posXLeft+=v;
        if(posXLeft > (widthDrawArea-sprite.getWidth()))
            v=-3;
        if(posXLeft < 0)
            v=3;
    }

    public void initializePosition(int surfaceHeigth)
    {
        int height = sprite.getHeight();
        posYTop = surfaceHeigth - height - surfaceHeigth*0.05f;

    }
}
