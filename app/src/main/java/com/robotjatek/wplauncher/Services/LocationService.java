package com.robotjatek.wplauncher.Services;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.CancellationSignal;

import java.util.concurrent.Executors;

public class LocationService {
    private static final long REFRESH_INTERVAL_MS = 10 * 60 * 1000;
    private boolean _hasPermission = false;
    private String _city = "";
    private long _lastRequest = 0;
    private final Context _context;

    public LocationService(Context context) {
        _context = context;
    }

    public String getCity() {
        if (!_hasPermission) {
            return "";
        }

        final var now = System.currentTimeMillis();
        if (now - _lastRequest > REFRESH_INTERVAL_MS) {
            getLocation();
        }

        return _city;
    }

    public boolean hasPermission() {
        return _hasPermission;
    }

    public void setHasPermission(boolean value) {
        _hasPermission = value;
        if (_hasPermission) {
            getLocation();
        }
    }

    private void getLocation() {
        final var now = System.currentTimeMillis();
        if (!_hasPermission || now - _lastRequest < REFRESH_INTERVAL_MS) {
            return;
        }

        final var lm = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
        lm.getCurrentLocation(LocationManager.NETWORK_PROVIDER, new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                (location -> extractCityName(location, _context)));

        _lastRequest = System.currentTimeMillis();
    }

    private void extractCityName(Location location, Context context) {
        if (location == null) {
            return;
        }

        final var gc = new Geocoder(context);
        gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1,
                addresses -> {
                    if (!addresses.isEmpty()) {
                        _city = addresses.get(0).getLocality();
                    }
                });
    }

}
