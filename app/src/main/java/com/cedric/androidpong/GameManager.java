package com.cedric.androidpong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Cedric on 19/03/2016.
 */
public abstract class GameManager {

    protected static final int SCORE_TO_WIN = 3;

    protected AppCompatActivity activity;
    protected GameSurfaceView surfaceViewManaged;
    protected Context surfaceContext;

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
            if(scoreOther == SCORE_TO_WIN)
            {
                onLose();
            }
        }
    }

    public abstract void notifyOtherBallLost();

    public abstract void sendBallToOther(float ballXPosition, float xVectorDirection, float yVectorDirection);

    protected void onOtherLostBall()
    {
        this.scoreThis += 1;
        if(scoreThis == SCORE_TO_WIN)
        {
            onWin();
        }
    }

    protected void onBallArrived(float ballXRelativePosition, float xVectorDirection, float yVectorDirection)
    {
        Log.i("BluetoothGamePongServic","ball"+ballXRelativePosition+", "+xVectorDirection+", "+yVectorDirection);
        Ball ball = new Ball(surfaceViewManaged.getResources(), ballXRelativePosition, 0, xVectorDirection, yVectorDirection);
        surfaceViewManaged.addObjectOnScene(ball);
    }

    public void onLose()
    {
        stopManagerOnceAndForAll();

        AlertDialog.Builder builder = new AlertDialog.Builder(surfaceContext);
        builder.setMessage("You WIN !");
        builder.setPositiveButton("Back to menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.show();
    }

    public void onWin()
    {
        stopManagerOnceAndForAll();

        AlertDialog.Builder builder = new AlertDialog.Builder(surfaceContext);
        builder.setMessage("You LOSE =(");
        builder.setPositiveButton("Back to menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.show();
    }

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onStop();

    public abstract void stopManagerOnceAndForAll();
}
