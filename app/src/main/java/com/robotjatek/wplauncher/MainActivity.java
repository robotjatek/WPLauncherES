package com.robotjatek.wplauncher;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;


public class MainActivity extends ComponentActivity {

    private LauncherSurfaceView _surface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _surface = new LauncherSurfaceView(this);
        _surface.setPreserveEGLContextOnPause(true);
        setContentView(_surface);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                _surface.onBackPressed();
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _surface.dispose();
    }
}