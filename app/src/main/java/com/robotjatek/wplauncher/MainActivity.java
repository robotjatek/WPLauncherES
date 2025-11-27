package com.robotjatek.wplauncher;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;


public class MainActivity extends Activity {

    private LauncherSurfaceView _surface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _surface = new LauncherSurfaceView(this);
        _surface.setPreserveEGLContextOnPause(true);
        setContentView(_surface);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _surface.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _surface.onResume();
    }
}