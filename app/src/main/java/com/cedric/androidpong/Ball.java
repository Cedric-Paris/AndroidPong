package com.cedric.androidpong;

import android.content.res.Resources;

/**
 * Created by Cedric on 07/03/2016.
 */
public class Ball extends GameObject{

    //private Vector2 direction;
    private float xVectorDirection = 5;
    private float yVectorDirection = 5;

    public Ball(Resources resources, float posXLeft, float posYTop)
    {
        super(resources, R.drawable.ball, posXLeft, posYTop);
    }

    public Ball(Resources resources)
    {
        super(resources, R.drawable.ball, 0, 0);
    }

    public void updateState(int widthDrawArea, int heightDrawArea)
    {
        posXLeft += xVectorDirection;
        posYTop += yVectorDirection;
        if(posXLeft > widthDrawArea || posXLeft < 0)
            rebondSurHorizontale();
        if(posYTop >heightDrawArea || posYTop < 0)
            rebondSurVerticale();
    }

    private void rebondSurHorizontale()
    {
        xVectorDirection = xVectorDirection * (-1);
    }

    private void rebondSurVerticale()
    {
        yVectorDirection = yVectorDirection * (-1);
    }


}
