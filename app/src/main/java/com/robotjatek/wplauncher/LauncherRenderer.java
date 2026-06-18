package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Services.AppChangeReceiver;
import com.robotjatek.wplauncher.Services.LocationService;
import com.robotjatek.wplauncher.Services.ScreenNavigator.ScreenNavigator;
import com.robotjatek.wplauncher.StartScreen.StartScreen;

public class LauncherRenderer implements GLSurfaceView.Renderer {
    public static ScreenData SCREEN_DATA = new ScreenData();
    private boolean _disposed = false;
    private float lastTime = System.nanoTime();
    private int frameCount = 0;
    private long fpsTime = System.currentTimeMillis();
    private final Context _context;
    private final float[] _projMatrix = new float[16];
    private final LocationService _locationService;
    private final AppChangeReceiver _appChangeReceiver;
    private Shader _shader;
    private QuadRenderer _renderer;
    private int _width, _height;
    private boolean _needsResize = false;
    private final LauncherSurfaceView _view;
    private final ScreenNavigator _navigator = new ScreenNavigator();

    public LauncherRenderer(Context context, LocationService locationService, AppChangeReceiver appChangeReceiver, LauncherSurfaceView view) {
        _context = context;
        _locationService = locationService;
        _appChangeReceiver = appChangeReceiver;
        _view = view;
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
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);

        if (_shader != null) {
            _shader.delete();
        }
        if (_renderer != null) {
            _renderer.dispose();
        }
        _shader = new Shader("","");
        _renderer = new QuadRenderer(_shader);
        _navigator.init(new StartScreen(_context, _navigator, _locationService, _appChangeReceiver, _view));
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
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT | GLES32.GL_STENCIL_BUFFER_BIT);
        GLES32.glDisable(GLES32.GL_STENCIL_TEST);
        _navigator.draw(delta, _projMatrix, _renderer);
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 glUnused, int width, int height) {
        _width = width;
        _height = height;
        updateLayout();
    }

    public void handleGesture(Gesture gesture) {
        _navigator.handleGesture(gesture.copyWithOffset(0, -SCREEN_DATA.topInset)); // corrigate screen coordinates with the insets
    }

    public void onBackPressed() {
        _navigator.onBackPressed();
    }

    public void onHomePressed() {
        _navigator.onHomePressed();
    }

    public void dispose() {
        if (!_disposed) {
            _navigator.dispose();
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

        var fov = 45f;
        var aspect = (float) _width / _height;
        var zNear = 10.0f;
        var zFar = 10000f;
        Matrix.perspectiveM(_projMatrix, 0, fov, aspect, zNear, zFar);

        // Distance to the Z=0 plane where 1 world unit = 1 screen pixel
        var distance = (float) ((_height / 2f) / Math.tan(Math.toRadians(fov / 2f)));

        var viewMatrix = new float[16];
        // Camera at -distance looking at 0.
        // With Up=(0,-1,0) and looking along +Z, world (0,0) is at screen Top-Left.
        Matrix.setLookAtM(viewMatrix, 0,
                _width / 2f, _height / 2f, -distance,
                _width / 2f, _height / 2f, 0f,
                0f, -1f, 0f);

        Matrix.multiplyMM(_projMatrix, 0, _projMatrix, 0, viewMatrix, 0);
        Matrix.translateM(_projMatrix, 0, 0, SCREEN_DATA.topInset, 0);
        _navigator.onResize(_width, _height);
    }
}
