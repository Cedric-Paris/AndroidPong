package com.cedric.androidpong.gameobject;

import android.content.res.Resources;
import android.graphics.RectF;

import com.cedric.androidpong.R;


/**
 * Created by Cedric on 07/03/2016.
 */
public class Ball extends GameObject
{

    private float xVectorDirection = 1;
    private float yVectorDirection = 1;

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

    public float getXVectorDirection() { return xVectorDirection; }

    public float getYVectorDirection() { return yVectorDirection; }

    public void setDirectionVector(float xVectorDirection, float yVectorDirection)
    {
        this.xVectorDirection = xVectorDirection;
        this.yVectorDirection = yVectorDirection;
    }

    public void updateState(int widthDrawArea, int heightDrawArea, Paddle mainPaddle, float sensorEventValue)
    {
        if(sprite == null)
            initializeSprite(appResources , R.drawable.ball);

        float heightScreenSpeed = heightDrawArea*0.00625f;//pour que le Vecteur direction ait le meme effet sur tout les ecrans -> calcul un vecteur relatif d'une certaine facon
        float widthSreenSpeed = widthDrawArea*0.01f;

        realXposition = (posXLeftRelative * widthDrawArea) + (xVectorDirection * widthSreenSpeed);
        realYposition = (posYTopRelative * heightDrawArea) + (yVectorDirection * heightScreenSpeed);

        if(realXposition > widthDrawArea-sprite.getHeight())
        {
            realXposition = widthDrawArea-sprite.getHeight();
            rebondSurVerticale();
        }
        if(realXposition < 0)
        {
            realXposition = 0;
            rebondSurVerticale();
        }
        if(this.getCollisionRect().intersect(mainPaddle.getCollisionRect()))
            manageCollisionWithPaddle(mainPaddle);
        if(realYposition >heightDrawArea || realYposition < - sprite.getHeight()){
            notifyObservers();
        }

        posXLeftRelative = getRelativePosition(realXposition, widthDrawArea);
        posYTopRelative = getRelativePosition(realYposition, heightDrawArea);
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
            realYposition = paddleRect.bottom;
            realXposition -= xVectorDirection * pourcentReduction;
            rebondSurHorizontale();
            return;
        }
        if(ballRect.bottom >= paddleRect.top)//Cas ou la balle arrive au dessus du paddle
        {
            pourcentReduction = ( (ballRect.bottom - paddleRect.top) * yVectorDirection ) / 100;
            realYposition = paddleRect.top - sprite.getHeight();
            realXposition -= xVectorDirection * pourcentReduction;
            rebondSurHorizontale();
            return;
        }
        ////A TRAITER Cas d'arrivee sur les cotes avec rebond different
    }

}
