package com.robotjatek.wplauncher.Services;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionService {

    private final LocationService _locationService;
    private final ActivityResultLauncher<String> _locationPermission;
    private final ComponentActivity _activity;

    public PermissionService(ComponentActivity activity, LocationService locationService) {
        _locationService = locationService;
        _activity = activity;
        _locationPermission = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), _locationService::setHasPermission);
    }

    public void ensureLocationPermission() {
        if (!hasLocationPermission()) {
            _locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            _locationService.setHasPermission(true);
        }
    }

    public void requestLocationPermission() {
        if (hasLocationPermission()) return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(_activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            _locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            openAppSettings();
        }
    }

    public void openAppSettings() {
        var intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", _activity.getPackageName(), null));
        _activity.startActivity(intent);
    }

    public void openNotificationSettings() {
        _activity.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    public void ensureNotificationPermission() {
        if (!hasNotificationAccess()) {
            _activity.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(_activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasNotificationAccess() {
        var notificationManager = (NotificationManager) _activity.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager != null && notificationManager.isNotificationListenerAccessGranted(
                new ComponentName(_activity, NotificationListener.class));
    }
}
