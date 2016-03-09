package com.cedric.androidpong;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

/**
 * Created by Cedric on 07/03/2016.
 */
public class SpriteRenderer {

    private Bitmap spriteBitmap;

    public SpriteRenderer(Resources resources, int indexResource)
    {
        spriteBitmap = BitmapFactory.decodeResource(resources, indexResource);
    }

    public void draw(Canvas canvas, float posXLeft, float PosYTop)
    {
        canvas.drawBitmap(spriteBitmap, posXLeft, PosYTop, null);
    }
}
