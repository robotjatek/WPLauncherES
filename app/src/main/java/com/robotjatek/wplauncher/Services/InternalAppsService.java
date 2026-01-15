package com.robotjatek.wplauncher.Services;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.InternalApps.Clock.Clock;
import com.robotjatek.wplauncher.InternalApps.Settings.Settings;
import com.robotjatek.wplauncher.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalAppsService {
    private static final String SETTINGS_NAME = "launcher:settings";
    private static final String CLOCK_NAME = "launcher:clock";
    private final Context _context;
    private final Map<String, Drawable> _appIcons = new HashMap<>();
    private final Map<String, App> _internalApps = new HashMap<>();
    private final IScreen _settingsScreen;
    private final IScreen _clockScreen;

    public InternalAppsService(Context context, SettingsService settings, IScreenNavigator navigator) {
        _context = context;
        initAppIcons();

        _settingsScreen = new Settings(navigator, settings, context);
        var setting = new App("Launcher Settings", SETTINGS_NAME, getAppIcon(SETTINGS_NAME),
                () -> navigator.push(_settingsScreen));
        _clockScreen = new Clock(navigator);
        var clock = new App("Clock", CLOCK_NAME, getAppIcon(CLOCK_NAME),
                () -> navigator.push(_clockScreen));

        _internalApps.put(SETTINGS_NAME, setting);
        _internalApps.put(CLOCK_NAME, clock);
    }

    public List<App> getInternalApps() {
        return _internalApps.values().stream().toList();
    }

    public App getInternalApp(String packageName) {
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
        _clockScreen.onResize(width, height);
    }
}
