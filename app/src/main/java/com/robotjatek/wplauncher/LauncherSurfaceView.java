package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.Services.AppChangeReceiver;
import com.robotjatek.wplauncher.Services.LocationService;

public class LauncherSurfaceView extends GLSurfaceView {

    private boolean _disposed = false;
    private final LauncherRenderer _renderer;
    private final GestureDetector _gestureDetector;

    public LauncherSurfaceView(Context context, LocationService locationService, AppChangeReceiver appChangeReceiver) {
        super(context);
        _renderer = new LauncherRenderer(context, locationService, appChangeReceiver);
        _gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
        {
           @Override
           public boolean onSingleTapUp(@NonNull MotionEvent e) {
               _renderer.handleGesture(new TapGesture(e.getX(), e.getY()));
               return true;
           }

           @Override
           public void onLongPress(@NonNull MotionEvent e) {
               _renderer.handleGesture(new LongPressGesture(e.getX(), e.getY()));
           }

           @Override
           public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
               _renderer.handleGesture(new ScrollGesture(e2.getX(), e2.getY(), distanceX, distanceY));
               return true;
           }


            @Override
           public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                                  float velocityX, float velocityY) {
               // TODO: renderer->handleGesture(new scrollgesture(x, y))
               return true;
           }
        });

        setEGLContextClientVersion(3);
        setRenderer(_renderer);

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        _gestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP -> _renderer.handleGesture(new UpGesture(x, y));
            case MotionEvent.ACTION_MOVE -> _renderer.handleGesture(new MoveGesture(x, y));
        }
        return true;
    }

    public void onBackPressed() {
        _renderer.onBackPressed();
    }

    public void onHomePressed() {
        _renderer.onHomePressed();
    }

    public LauncherRenderer getRenderer() {
        return _renderer;
    }

    public void dispose() {
        if (!_disposed) {
            _renderer.dispose();
            _disposed = true;
        }
    }
}
