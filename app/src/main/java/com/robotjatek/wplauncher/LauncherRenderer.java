package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Services.LocationService;
import com.robotjatek.wplauncher.StartPage.StartScreen;

import java.util.ArrayDeque;
import java.util.Deque;

public class LauncherRenderer implements GLSurfaceView.Renderer, IScreenNavigator {
    private float lastTime = System.nanoTime();
    private int frameCount = 0;
    private long fpsTime = System.currentTimeMillis();

    private final Deque<IScreen> _navigationStack = new ArrayDeque<>();
    private final Context _context;
    private final float[] _projMatrix = new float[16];
    private int _topInset = 0;
    private final LocationService _locationService;

    public LauncherRenderer(Context context, LocationService locationService) {
        _context = context;
        _locationService = locationService;
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 glUnused,
                                 javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glFrontFace(GLES20.GL_CW);
        // Init screens in surfaceCreated so no accidental gl calls before the surface is ready
        _navigationStack.push(new StartScreen(_context, this, _locationService));
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 glUnused) {
        var now = System.nanoTime();
        var delta = (now - lastTime) / 1000000f;
        lastTime = now;

        frameCount++;
        if (System.currentTimeMillis() - fpsTime >= 1000) {
            frameCount = 0;
            fpsTime = System.currentTimeMillis();
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        _navigationStack.getFirst().draw(delta, _projMatrix); // TODO: animation
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.orthoM(_projMatrix, 0, 0, width, height, 0, -1, 1);
        Matrix.translateM(_projMatrix, 0, 0, _topInset, 0);
        _navigationStack.forEach(s -> s.onResize(width, height));
    }

    public void handleTouchDown(float x, float y) {
        _navigationStack.getFirst().onTouchStart(x, y - _topInset);
    }

    public void handleTouchUp(float x, float y) {
        _navigationStack.getFirst().onTouchEnd(x, y - _topInset);
    }

    public void handleTouchMove(float x, float y) {
        _navigationStack.getFirst().onTouchMove(x, y - _topInset);
    }

    public void onBackPressed() {
        _navigationStack.getFirst().onBackPressed();
    }

    @Override
    public void push(IScreen screen) {
        _navigationStack.push(screen);
    }

    @Override
    public void pop() {
        _navigationStack.pop();
    }

    public void dispose() {
        _navigationStack.forEach(IScreen::dispose);
    }

    public void setInsets(int left, int top, int right, int bottom) {
        _topInset = top;
    }
}
