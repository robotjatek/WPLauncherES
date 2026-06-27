package com.robotjatek.wplauncher;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
import static android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import com.robotjatek.wplauncher.Services.AppChangeReceiver;
import com.robotjatek.wplauncher.Services.LocationService;
import com.robotjatek.wplauncher.Services.PermissionService;

import java.util.List;


public class MainActivity extends ComponentActivity {

    private LauncherSurfaceView _surface;
    private final LocationService _locationService = new LocationService(this);
    private final AppChangeReceiver _appChangeReceiver = new AppChangeReceiver();
    private final PermissionService _permissionService = new PermissionService(this, _locationService);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        EdgeToEdge.enable(this);

        _surface = new LauncherSurfaceView(this, _locationService, _permissionService, _appChangeReceiver);
        _surface.setPreserveEGLContextOnPause(true);

        ViewCompat.setWindowInsetsAnimationCallback(getWindow().getDecorView(),
                new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                        return insets;
                    }

                    @Override
                    public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                        var rootInsets = ViewCompat.getRootWindowInsets(getWindow().getDecorView());
                        if (rootInsets != null && !rootInsets.isVisible(WindowInsetsCompat.Type.ime())) {
                            _surface.cancelFocus();
                        }
                    }
                });

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

        _permissionService.ensureLocationPermission();
        _permissionService.ensureNotificationPermission();
        setupAppChangeListener();
    }

    private void setupAppChangeListener() {
        var filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        registerReceiver(_appChangeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _surface.onPause();
        _locationService.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _surface.onResume();
        _surface.onHomePressed();
        _locationService.resume();
    }

    @Override
    protected void onDestroy() {
        Log.d(MainActivity.class.getName(), "onDestroy!!!!");
        super.onDestroy();
        _surface.dispose();
        if (_appChangeReceiver != null) {
            unregisterReceiver(_appChangeReceiver);
        }
    }
}