package com.robotjatek.wplauncher;

import android.Manifest;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.robotjatek.wplauncher.Services.LocationService;


public class MainActivity extends ComponentActivity {

    private LauncherSurfaceView _surface;
    private ActivityResultLauncher<String> _locationPermission;
    private final LocationService _locationService = LocationService.create(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _locationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                _locationService::setHasPermission
        );
        ensureLocationPermission();

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

    private void ensureLocationPermission() {
        if (!_locationService.hasPermission()) {
            _locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
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