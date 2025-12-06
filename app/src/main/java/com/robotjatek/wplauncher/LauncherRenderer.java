package com.robotjatek.wplauncher;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.robotjatek.wplauncher.StartPage.StartScreen;

public class LauncherRenderer implements GLSurfaceView.Renderer {
    private float lastTime = System.nanoTime();

    private int frameCount = 0;
    private long fpsTime = System.currentTimeMillis();

    private StartScreen _startScreen;

    public LauncherRenderer() {
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 glUnused,
                                 javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        _startScreen = new StartScreen(); // Init startscreen here so no accidental gl calls before the surface is ready
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 glUnused) {
        var now = System.nanoTime();
        var delta = (now - lastTime) / 1000000f;
        lastTime = now;

        frameCount++;
        if (System.currentTimeMillis() - fpsTime > 1000) {
            frameCount = 0;
            fpsTime = System.currentTimeMillis();
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        _startScreen.draw(delta);
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        _startScreen.onResize(width, height);

    }

    public void handleTouchDown(float x, float y) {
        _startScreen.onTouchStart(x, y);
    }

    public void handleTouchUp(float x, float y) {
        _startScreen.onTouchEnd(x, y);
    }

    public void handleTouchMove(float x, float y) {
        _startScreen.onTouchMove(x, y);
    }

    public void handleTouchCancel() {
    }
}
