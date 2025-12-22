package com.robotjatek.wplauncher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class BitmapUtil {

    /**
     * Converts a drawable (possibly an application icon) to an OpenGL texture.
     * Do not call this method off-main-thread!
     * @return Texture handle >0 on SUCCESS, <1 otherwise
     */
    public static int createTextureFromDrawable(Drawable drawable, int width, int height) {
        var bitmap = toBitmap(drawable, width, height);
        var texId = createTextureFromBitmap(bitmap);
        bitmap.recycle();
        return texId;
    }

    private static Bitmap toBitmap(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable bitmapDrawable) {
            if (bitmapDrawable.getBitmap() != null) {
                return Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, true);
            }
        }

        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        canvas.drawColor(0x00000000);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private static int createTextureFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return -1;
        }

        final int[] ids = new int[1];
        GLES20.glGenTextures(1, ids, 0);
        if (ids[0] == 0) {
            throw new RuntimeException("failed to create texture");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        return ids[0];
    }
}
