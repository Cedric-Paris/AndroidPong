package com.cedric.androidpong.game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cedric.androidpong.R;
import com.cedric.androidpong.gameobject.Ball;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cedric on 19/03/2016.
 */
public abstract class GameManager {

    protected static final int SCORE_TO_WIN = 10;
    protected static final long TIME_BEFORE_BALL_SPAWN = 7000;

    protected AppCompatActivity activity;
    protected GameSurfaceView surfaceViewManaged;
    protected Context surfaceContext;

    protected int scoreOther;
    protected int scoreThis;

    protected int nbBallOnThisScene = 1;
    protected Random random = new Random();

    protected TimerTask spawnBallTask;
    private volatile boolean isTaskRunning;

    public int getScoreThis() { return scoreThis; }

    public int getScoreOther() { return scoreOther; }

    public abstract void saveInstanceState(Bundle bundle);

    public void onBallDestroyed(Ball ballDestroyed)
    {
        this.nbBallOnThisScene -= 1;
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
            if(nbBallOnThisScene <= 0)
            {
                Ball ball = new Ball(surfaceViewManaged.getResources(), random.nextFloat(),0, random.nextFloat(), 1.0f);
                surfaceViewManaged.addObjectOnScene(ball);
                nbBallOnThisScene = 1;
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
        this.nbBallOnThisScene += 1;
        Ball ball = new Ball(surfaceViewManaged.getResources(), ballXRelativePosition, 0, xVectorDirection, yVectorDirection);
        surfaceViewManaged.addObjectOnScene(ball);
    }

    protected synchronized void startBallSpawnTask()
    {
        if(isTaskRunning){
            return;
        }
        isTaskRunning = true;
        spawnBallTask = new TimerTask() {
            @Override
            public void run() {
                Ball ball = new Ball(surfaceViewManaged.getResources(), random.nextFloat(),0, random.nextFloat(), 1.0f);
                surfaceViewManaged.addObjectOnScene(ball);
                nbBallOnThisScene += 1;
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(spawnBallTask, TIME_BEFORE_BALL_SPAWN / 2, TIME_BEFORE_BALL_SPAWN);
    }

    protected void stopBallSpawnTask()
    {
        if(spawnBallTask != null) {
            spawnBallTask.cancel();
        }
        isTaskRunning = false;
    }

    public void onLose()
    {
        stopBallSpawnTask();
        stopManagerOnceAndForAll();

        AlertDialog.Builder builder = new AlertDialog.Builder(surfaceContext);
        builder.setMessage(R.string.textMessageLose);
        builder.setPositiveButton(R.string.textButtonBackMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.show();
    }

    public void onWin()
    {
        stopBallSpawnTask();
        stopManagerOnceAndForAll();

        AlertDialog.Builder builder = new AlertDialog.Builder(surfaceContext);

        builder.setMessage(R.string.textMessageWin);
        builder.setPositiveButton(R.string.textButtonBackMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.show();
    }

    public abstract void start();

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onStop();

    public abstract void stopManagerOnceAndForAll();
}
