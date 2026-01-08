package com.robotjatek.wplauncher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.util.Objects;

public class BitmapUtil {

    // https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive
    private static final float ADAPTIVE_ICON_SIZE_DP = 108f;
    private static final float ADAPTIVE_ICON_SAFE_ZONE_DP = 66f;
    private static final float ADAPTIVE_ICON_SCALE = ADAPTIVE_ICON_SIZE_DP / ADAPTIVE_ICON_SAFE_ZONE_DP;

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

    public static Bitmap createRect(int width, int height, int padding, int color) {
        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        canvas.drawColor(0xff000000);
        var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        canvas.drawRect(padding, padding, width - padding, height - padding, paint);
        return bitmap;
    }

    private static Bitmap toBitmap(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable bitmapDrawable) {
            var bitmap = bitmapDrawable.getBitmap();
            if (bitmapDrawable.getBitmap() != null) {
                return Bitmap.createScaledBitmap(bitmap.copy(Objects.requireNonNull(bitmap.getConfig()), false), width, height, true);
            }
        }

        if (drawable instanceof AdaptiveIconDrawable adaptive) {
            var monochrome = adaptive.getMonochrome();
            if (monochrome != null) {
                monochrome = monochrome.mutate();
                var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                var canvas = new Canvas(bitmap);
                canvas.drawColor(0);
                canvas.scale(ADAPTIVE_ICON_SCALE, ADAPTIVE_ICON_SCALE,
                        width / 2f, height / 2f);
                monochrome.setBounds(0, 0, width, height);
                monochrome.setTint(Colors.WHITE);
                monochrome.draw(canvas);
                return bitmap;
            }
        }

        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        canvas.drawColor(0x00000000);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static int createTextureFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return -1;
        }
        final var ids = new int[1];
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
