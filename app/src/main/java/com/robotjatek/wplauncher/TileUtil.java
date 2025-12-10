package com.robotjatek.wplauncher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;

// TODO: place icon on the tile
public class TileUtil {
    public static int createTextTexture(String text, int width, int height, int textColor) {

        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);

        var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.CENTER);

        var textSize = height * 0.6f;
        paint.setTextSize(textSize);

        var textWidth = paint.measureText(text);
        // scale down here?

        var fontMetrics = paint.getFontMetrics();
        var x = width / 2f;
        var y = height / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f;
        canvas.drawColor(0xff0000ff); // TODO: pass color as param
        canvas.drawText(text, x, y, paint);

        var ids = new int[1];
        GLES20.glGenTextures(1, ids, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();

        return ids[0];
    }
}
