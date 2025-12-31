package com.robotjatek.wplauncher;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.InternalApps.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalAppsService {
    private static final String SETTINGS_NAME = "launcher:settings";
    private final Context _context;
    private final Map<String, Drawable> _appIcons = new HashMap<>();
    private final Map<String, App> _internalApps = new HashMap<>();
    private final IScreen _settingsScreen;

    public InternalAppsService(Context context, IScreenNavigator navigator) {
        _context = context;
        initAppIcons();

        _settingsScreen = new Settings(navigator);
        var setting = new App("Launcher Settings", SETTINGS_NAME, getAppIcon(SETTINGS_NAME),
                () -> navigator.push(_settingsScreen));

        _internalApps.put(SETTINGS_NAME, setting);
    }

    public List<App> getInternalApps() {
        return _internalApps.values().stream().toList();
    }

    public App getApp(String packageName) {
        return _internalApps.get(packageName);
    }

    private Drawable getAppIcon(String packageName) {
        return _appIcons.getOrDefault(packageName,
                ContextCompat.getDrawable(_context, R.drawable.close_circle));
    }

    private void initAppIcons() {
        _appIcons.put(SETTINGS_NAME, ContextCompat.getDrawable(_context, R.drawable.settings));
    }

    public void dispose() {
        _settingsScreen.dispose();
    }

    public void onSizeChanged(int width, int height) {
        _settingsScreen.onResize(width, height);
    }
}
