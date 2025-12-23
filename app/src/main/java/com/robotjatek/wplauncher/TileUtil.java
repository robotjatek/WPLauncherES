package com.robotjatek.wplauncher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class TileUtil {

    private static final int CONTENT_PADDING_PX = 20;

    public static int createTextTexture(String text, int width, int height, int textSize,
                                        int typefaceStyle, int textColor, int bgColor, VerticalAlign textAlign) {

        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        canvas.drawColor(bgColor);

        var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("sans-serif-light", typefaceStyle));
        paint.setTextSize(textSize);
        //var textWidth = paint.measureText(text);

        switch (textAlign) {
            case CENTER -> {
                var fm = paint.getFontMetrics();
                var textHeight = fm.descent - fm.ascent;
                var y = (height - textHeight) / 2 - fm.ascent;
                canvas.drawText(text, CONTENT_PADDING_PX, y, paint);
            }
            case BOTTOM -> canvas.drawText(text, CONTENT_PADDING_PX, height - CONTENT_PADDING_PX, paint);
        }

        var ids = new int[1];
        GLES20.glGenTextures(1, ids, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();

        if (ids[0] < 1) {
            throw new RuntimeException("Failed to create a texture. This may be an off main-thread call");
        }

        return ids[0];
    }

    public static void deleteTexture(int handle) {
        if (handle != -1) {
            GLES20.glDeleteTextures(1, new int[] {handle}, 0);
        }
    }
}
