package com.robotjatek.wplauncher;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
import static android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;

import android.Manifest;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.robotjatek.wplauncher.Services.LocationService;


public class MainActivity extends ComponentActivity {

    private LauncherSurfaceView _surface;
    private ActivityResultLauncher<String> _locationPermission;
    private final LocationService _locationService = LocationService.create(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        _locationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                _locationService::setHasPermission);

        _surface = new LauncherSurfaceView(this);
        _surface.setPreserveEGLContextOnPause(true);
        ViewCompat.setOnApplyWindowInsetsListener(_surface, (view, insets) -> {
            var sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            _surface.getRenderer().setInsets(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });
        var controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.setSystemBarsAppearance(
                    0, APPEARANCE_LIGHT_STATUS_BARS | APPEARANCE_LIGHT_NAVIGATION_BARS);
        }

        setContentView(_surface);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                _surface.onBackPressed();
            }
        });

        ensureLocationPermission();
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