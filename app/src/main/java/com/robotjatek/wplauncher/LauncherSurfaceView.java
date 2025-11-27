package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class LauncherSurfaceView extends GLSurfaceView {

    private final LauncherRenderer _renderer;

    public LauncherSurfaceView(Context context) {
        super(context);
        _renderer = new LauncherRenderer();

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
            case MotionEvent.ACTION_CANCEL -> _renderer.handleTouchCancel();
        }
        return true;
    }
}
