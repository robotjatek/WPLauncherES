package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.robotjatek.wplauncher.Services.LocationService;

public class LauncherSurfaceView extends GLSurfaceView {

    private final LauncherRenderer _renderer;

    public LauncherSurfaceView(Context context, LocationService locationService) {
        super(context);
        _renderer = new LauncherRenderer(context, locationService);

        setEGLContextClientVersion(2);
        setRenderer(_renderer);

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> _renderer.handleTouchDown(x, y);
            case MotionEvent.ACTION_UP -> _renderer.handleTouchUp(x, y);
            case MotionEvent.ACTION_MOVE -> _renderer.handleTouchMove(x, y);
        }
        return true;
    }

    public void onBackPressed() {
        _renderer.onBackPressed();
    }

    public LauncherRenderer getRenderer() {
        return _renderer;
    }

    public void dispose() {
        _renderer.dispose();
    }
}
