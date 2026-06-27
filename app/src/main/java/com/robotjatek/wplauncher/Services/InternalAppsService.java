package com.robotjatek.wplauncher.Services;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.InternalApps.Clock.Clock;
import com.robotjatek.wplauncher.InternalApps.Settings.Settings;
import com.robotjatek.wplauncher.R;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalAppsService {
    private static final String SETTINGS_NAME = "launcher:settings";
    private static final String CLOCK_NAME = "launcher:clock";
    private boolean _disposed = false;
    private final Context _context;
    private final Map<String, Drawable> _appIcons = new HashMap<>();
    private final Map<String, App> _internalApps = new HashMap<>();

    public InternalAppsService(Context context, SettingsService settings, PermissionService permissionService, IScreenNavigator navigator) {
        _context = context;
        initAppIcons();

        var setting = new App(
                "Launcher Settings",
                SETTINGS_NAME,
                null,
                getAppIcon(SETTINGS_NAME),
                () -> navigator.push(new Settings(navigator, settings, permissionService, context)), true);

        var clock = new App(
                "Clock HUB",
                CLOCK_NAME,
                null,
                getAppIcon(CLOCK_NAME),
                () -> navigator.push(new Clock(navigator, context)), true);

        _internalApps.put(SETTINGS_NAME, setting);
        _internalApps.put(CLOCK_NAME, clock);
    }

    public List<App> getInternalApps() {
        return _internalApps.values().stream()
                .sorted(Comparator.comparing(App::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
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
        _appIcons.put(CLOCK_NAME, ContextCompat.getDrawable(_context, R.drawable.clock));
    }

    public void dispose() {
        if (!_disposed) {
            _disposed = true;
        }
    }
}
