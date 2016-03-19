package com.cedric.androidpong;

import android.os.Bundle;
import android.util.Log;

/**
 * Created by Cedric on 19/03/2016.
 */
public abstract class GameManager {

    protected GameSurfaceView surfaceViewManaged;

    protected int scoreOther;
    protected int scoreThis;

    public int getScoreThis() { return scoreThis; }

    public int getScoreOther() { return scoreOther; }

    public abstract void saveInstanceState(Bundle bundle);

    public void onBallDestroyed(Ball ballDestroyed)
    {
        if(ballDestroyed.getPosYTopRelative()<=0)
        {
            sendBallToOther(ballDestroyed.getPosXLeftRelative(), ballDestroyed.getYVectorDirection() * (-1), ballDestroyed.getYVectorDirection() * (-1));
        }
        else
        {
            this.scoreOther += 1;
            notifyOtherBallLost();
        }
    }

    public abstract void notifyOtherBallLost();

    public abstract void sendBallToOther(float ballXPosition, float xVectorDirection, float yVectorDirection);

    protected void onOtherLostBall()
    {
        this.scoreThis += 1;
    }

    protected void onBallArrived(float ballXRelativePosition, float xVectorDirection, float yVectorDirection)
    {
        Log.i("BluetoothGamePongServic","ball"+ballXRelativePosition+", "+xVectorDirection+", "+yVectorDirection);
        Ball ball = new Ball(surfaceViewManaged.getResources(), ballXRelativePosition, 0, xVectorDirection, yVectorDirection);
        surfaceViewManaged.addObjectOnScene(ball);
    }
}
