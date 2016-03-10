package com.cedric.androidpong;

import android.content.res.Resources;
import android.graphics.RectF;


/**
 * Created by Cedric on 07/03/2016.
 */
public class Ball extends GameObject
{

    private float xVectorDirection = 5;
    private float yVectorDirection = 5;

    public Ball(Resources resources, float posXLeft, float posYTop, float xDirection, float yDirection)
    {
        super(resources, R.drawable.ball, posXLeft, posYTop);
        setDirectionVector(xDirection, yDirection);
    }

    public Ball(Resources resources, float posXLeft, float posYTop)
    {
        super(resources, R.drawable.ball, posXLeft, posYTop);
    }

    public Ball(Resources resources)
    {
        super(resources, R.drawable.ball, 0, 0);
    }

    public void setDirectionVector(float xVectorDirection, float yVectorDirection)
    {
        this.xVectorDirection = xVectorDirection;
        this.yVectorDirection = yVectorDirection;
    }

    public void updateState(int widthDrawArea, int heightDrawArea, Paddle mainPaddle)
    {
        posXLeft += xVectorDirection;
        posYTop += yVectorDirection;
        if(posXLeft > widthDrawArea-sprite.getHeight() || posXLeft < 0)
            rebondSurVerticale();
        if(posYTop < 0)
            rebondSurHorizontale();
        if(this.getCollisionRect().intersect(mainPaddle.getCollisionRect()))
            manageCollisionWithPaddle(mainPaddle);
        if(posYTop >heightDrawArea){
            notifyObservers();
        }

    }

    private void rebondSurVerticale()
    {
        xVectorDirection = xVectorDirection * (-1);
    }

    private void rebondSurHorizontale()
    {
        yVectorDirection = yVectorDirection * (-1);
    }

    private void manageCollisionWithPaddle(Paddle mainPaddle)
    {
        float pourcentReduction;
        RectF paddleRect = mainPaddle.getCollisionRect();
        RectF ballRect = this.getCollisionRect();
        if(ballRect.top >= paddleRect.top)//Cas ou la balle arrive en dessous du paddle
        {
            pourcentReduction = ( (ballRect.top - paddleRect.top) * yVectorDirection ) / 100;
            posYTop = paddleRect.bottom;
            posXLeft -= xVectorDirection * pourcentReduction;
            rebondSurHorizontale();
            return;
        }
        if(ballRect.bottom >= paddleRect.top)//Cas ou la balle arrive au dessus du paddle
        {
            pourcentReduction = ( (ballRect.bottom - paddleRect.top) * yVectorDirection ) / 100;
            posYTop = paddleRect.top - sprite.getHeight();
            posXLeft -= xVectorDirection * pourcentReduction;
            rebondSurHorizontale();
            return;
        }
        ////A TRAITER Cas d'arrivee sur les cotes avec rebond different
    }

}
