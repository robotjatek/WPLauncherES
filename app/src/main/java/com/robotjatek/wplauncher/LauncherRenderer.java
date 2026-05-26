package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import androidx.annotation.NonNull;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Services.AppChangeReceiver;
import com.robotjatek.wplauncher.Services.LocationService;
import com.robotjatek.wplauncher.StartScreen.StartScreen;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LauncherRenderer implements GLSurfaceView.Renderer, IScreenNavigator {
    public static ScreenData SCREEN_DATA = new ScreenData();
    private boolean _disposed = false;
    private float lastTime = System.nanoTime();
    private int frameCount = 0;
    private long fpsTime = System.currentTimeMillis();

    private final Deque<IScreen> _navigationStack = new ConcurrentLinkedDeque<>();
    private final Context _context;
    private final float[] _projMatrix = new float[16];
    private final LocationService _locationService;
    private final AppChangeReceiver _appChangeReceiver;
    private Shader _shader;
    private QuadRenderer _renderer;
    private int _width, _height;
    private boolean _needsResize = false;

    public LauncherRenderer(Context context, LocationService locationService, AppChangeReceiver appChangeReceiver) {
        _context = context;
        _locationService = locationService;
        _appChangeReceiver = appChangeReceiver;
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 glUnused,
                                 javax.microedition.khronos.egl.EGLConfig config) {
        Thread.currentThread().setUncaughtExceptionHandler(new CrashHandler(_context.getApplicationContext())); // Log GL thread crashes as well
        // Init screens and every GL related objects in surfaceCreated so no accidental gl calls before the surface is ready
        GLES32.glClearColor(0f, 0f, 0f, 1f);
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        GLES32.glFrontFace(GLES32.GL_CW);
        GLES32.glCullFace(GLES32.GL_BACK);

        if (_shader != null) {
            _shader.delete();
        }
        if (_renderer != null) {
            _renderer.dispose();
        }

        while (!_navigationStack.isEmpty()) {
            _navigationStack.pop().dispose();
        }

        _shader = new Shader("","");
        _renderer = new QuadRenderer(_shader);
        _navigationStack.push(new StartScreen(_context, this, _locationService, _appChangeReceiver));
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 glUnused) {
        if (_needsResize && _width > 0 && _height > 0) {
            updateLayout();
            _needsResize = false;
        }

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
        _width = width;
        _height = height;
        updateLayout();
    }

    public void handleGesture(Gesture gesture) {
        if (!_navigationStack.isEmpty()) {
            _navigationStack.getFirst().handleGesture(gesture.copyWithOffset(0, -SCREEN_DATA.topInset)); // corrigate screen coordinates with the insets
        }
    }

    public void onBackPressed() {
        _navigationStack.getFirst().onBackPressed();
    }

    public void onHomePressed() {
        if (!_navigationStack.isEmpty()) {
            while (_navigationStack.size() > 1) {
                _navigationStack.pop().dispose();
            }
            var startScreen = _navigationStack.getLast();
            startScreen.onBackPressed();
        }
    }

    @Override
    public void push(@NonNull IScreen screen) {
        screen.onResize(SCREEN_DATA.screenWidth, SCREEN_DATA.screenHeight);
        _navigationStack.push(screen);
    }

    @Override
    public void pop() {
        var current = _navigationStack.peek();
        _navigationStack.pop();
        if (current != null) {
            current.dispose();
        }
    }

    public void dispose() {
        if (!_disposed) {
            _navigationStack.forEach(IScreen::dispose);
            _navigationStack.clear();
            if (_renderer != null) _renderer.dispose();
            if (_shader != null) _shader.delete();
            _disposed = true;
        }
    }

    public void setInsets(int left, int top, int right, int bottom) {
        if (SCREEN_DATA.topInset != top || SCREEN_DATA.bottomInset != bottom) {
            SCREEN_DATA.topInset = top;
            SCREEN_DATA.bottomInset = bottom;
            _needsResize = true;
        }
    }

    private void updateLayout() {
        if (_width <= 0 || _height <= 0) {
            return;
        }

        SCREEN_DATA.screenHeight = _height;
        SCREEN_DATA.screenWidth = _width;
        GLES32.glViewport(0, 0, _width, _height);
        Matrix.orthoM(_projMatrix, 0, 0, _width, _height, 0, -1, 1);
        Matrix.translateM(_projMatrix, 0, 0, SCREEN_DATA.topInset, 0);
        _navigationStack.forEach(s -> s.onResize(_width, _height));
    }
}
