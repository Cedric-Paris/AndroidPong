package com.cedric.androidpong;

import android.content.res.Resources;

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
        super(resources, R.drawable.paddle_sprite, 0, 0);
    }

    public void updateState(int widthDrawArea, int heightDrawArea)
    {
        posXLeft+=1;
        posYTop+=1;
    }
}
