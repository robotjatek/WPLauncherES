package com.robotjatek.wplauncher.Services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.CancellationSignal;

import androidx.core.app.ActivityCompat;

import java.util.concurrent.Executors;

public class LocationService {
    private static final long REFRESH_INTERVAL_MS = 10 * 60 * 1000;
    private boolean _hasPermission = false;
    private boolean _isPaused = false;
    private String _city = "";
    private Location _location;
    private long _lastRequest = 0;
    private final Context _context;

    public LocationService(Context context) {
        _context = context;
    }

    public void pause() {
        _isPaused = true;
    }

    public void resume() {
        _isPaused = false;
    }

    public String getCity() {
        if (!hasPermission()) {
            return "";
        }

        final var now = System.currentTimeMillis();
        if (now - _lastRequest > REFRESH_INTERVAL_MS) {
            queryLocation();
        }

        return _city;
    }

    public boolean hasPermission() {
        return _hasPermission;
    }

    public void setHasPermission(boolean value) {
        _hasPermission = value;
        if (hasPermission()) {
            queryLocation();
        }
    }

    public Location getLocation() {
        queryLocation();
        return _location;
    }

    private void queryLocation() {
        final var now = System.currentTimeMillis();
        if (!hasPermission() || now - _lastRequest < REFRESH_INTERVAL_MS || _isPaused) {
            return;
        }

        final var lm = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.getCurrentLocation(LocationManager.NETWORK_PROVIDER, new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                (location -> extractCityName(location, _context)));

        _lastRequest = System.currentTimeMillis();
    }

    private void extractCityName(Location location, Context context) {
        if (location == null) {
            return;
        }
        _location = location;
        final var gc = new Geocoder(context);
        gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1,
                addresses -> {
                    if (!addresses.isEmpty()) {
                        _city = addresses.get(0).getLocality();
                    }
                });
    }

}
