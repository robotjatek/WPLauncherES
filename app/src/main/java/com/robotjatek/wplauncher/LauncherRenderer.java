package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Services.AppChangeReceiver;
import com.robotjatek.wplauncher.Services.LocationService;
import com.robotjatek.wplauncher.StartPage.StartScreen;

import java.util.ArrayDeque;
import java.util.Deque;

public class LauncherRenderer implements GLSurfaceView.Renderer, IScreenNavigator {
    private boolean _disposed = false;
    private float lastTime = System.nanoTime();
    private int frameCount = 0;
    private long fpsTime = System.currentTimeMillis();

    private final Deque<IScreen> _navigationStack = new ArrayDeque<>();
    private final Context _context;
    private final float[] _projMatrix = new float[16];
    private int _topInset = 0;
    private final LocationService _locationService;
    private final AppChangeReceiver _appChangeReceiver;
    private Shader _shader;
    private QuadRenderer _renderer;

    public LauncherRenderer(Context context, LocationService locationService, AppChangeReceiver appChangeReceiver) {
        _context = context;
        _locationService = locationService;
        _appChangeReceiver = appChangeReceiver;
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 glUnused,
                                 javax.microedition.khronos.egl.EGLConfig config) {
        // Init screens and every GL related objects in surfaceCreated so no accidental gl calls before the surface is ready
        GLES32.glClearColor(0f, 0f, 0f, 1f);
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        GLES32.glFrontFace(GLES32.GL_CW);
        GLES32.glCullFace(GLES32.GL_BACK);
        _shader = new Shader("","");
        _renderer = new QuadRenderer(_shader);
        _navigationStack.push(new StartScreen(_context, this, _locationService, _appChangeReceiver));
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
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT);
        _navigationStack.getFirst().draw(delta, _projMatrix, _renderer); // TODO: animation
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 glUnused, int width, int height) {
        GLES32.glViewport(0, 0, width, height);
        Matrix.orthoM(_projMatrix, 0, 0, width, height, 0, -1, 1);
        Matrix.translateM(_projMatrix, 0, 0, _topInset, 0);
        _navigationStack.forEach(s -> s.onResize(width, height));
    }

    public void handleGesture(Gesture gesture) {
        if (!_navigationStack.isEmpty()) {
            _navigationStack.getFirst().handleGesture(gesture.copyWithOffset(0, -_topInset));
        }
    }

    public void onBackPressed() {
        _navigationStack.getFirst().onBackPressed();
    }

    public void onHomePressed() {
        if (!_navigationStack.isEmpty()) {
            var startScreen = _navigationStack.getLast();
            _navigationStack.clear();
            _navigationStack.add(startScreen);
            startScreen.onBackPressed(); // TODO: animated scroll to top
        }
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
        if (!_disposed) {
            _navigationStack.forEach(IScreen::dispose);
            _renderer.dispose();
            _shader.delete();
            _disposed = true;
        }
    }

    public void setInsets(int left, int top, int right, int bottom) {
        _topInset = top;
    }
}
