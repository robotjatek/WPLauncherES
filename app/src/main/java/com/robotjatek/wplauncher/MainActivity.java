package com.robotjatek.wplauncher;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends ComponentActivity {

    private LauncherSurfaceView _surface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _surface = new LauncherSurfaceView(this);
        _surface.setPreserveEGLContextOnPause(true);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(_surface, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            _surface.getRenderer().setInsets(sys.left, sys.top, sys.right, sys.bottom);

            return insets;
        });
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