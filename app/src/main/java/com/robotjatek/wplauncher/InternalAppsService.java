package com.robotjatek.wplauncher;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.robotjatek.wplauncher.AppList.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalAppsService {
    private static final String SETTINGS_NAME = "launcher:settings";
    private final Context _context;
    private final Map<String, Drawable> _appIcons = new HashMap<>();
    private final List<App> _internalApps = new ArrayList<>();

    public InternalAppsService(Context context) {
        _context = context;
        initAppIcons();

        var setting = new App("Launcher Settings", SETTINGS_NAME, getAppIcon(SETTINGS_NAME),  () -> {});
        _internalApps.add(setting);
    }

    public List<App> getInternalApps() {
        return _internalApps;
    }

    public App getApp(String title, String packageName) {
        return new App(title, packageName, getAppIcon(packageName), () -> {});
    }

    private Drawable getAppIcon(String packageName) {
        return _appIcons.getOrDefault(packageName,
                ContextCompat.getDrawable(_context, R.drawable.close_circle));
    }

    private void initAppIcons() {
        _appIcons.put(SETTINGS_NAME, ContextCompat.getDrawable(_context, R.drawable.settings));
    }
}
