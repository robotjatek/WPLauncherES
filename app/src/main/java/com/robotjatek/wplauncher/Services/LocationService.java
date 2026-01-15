package com.robotjatek.wplauncher.Services;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

public class LocationService {
    private boolean _hasPermission = false;
    private static LocationService _instance;
    private String _city = "";

    public static LocationService create() {
        if (_instance == null) {
            _instance = new LocationService();
        }
        return _instance;
    }

    public static LocationService get() {
        return _instance;
    }

    public String getCity() {
        if (!_hasPermission) {
            return "";
        }

        return _city;
    }

    public boolean hasPermission() {
        return _hasPermission;
    }

    public void setHasPermission(boolean value, Context context) {
        _hasPermission = value;
        if (_hasPermission) {
            startGetLocation(context);
        }
    }

    private void startGetLocation(Context context) {
        if (!_hasPermission) {
            return;
        }
        final var lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 100f,
                (Location loc) -> extractCityName(loc, context));
    }

    private void extractCityName(Location location, Context context) {
        var gc = new Geocoder(context);
        gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1,
                addresses -> {
                    if (!addresses.isEmpty()) {
                        _city = addresses.get(0).getLocality();
                    }
                });
    }

}
