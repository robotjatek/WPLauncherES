package com.robotjatek.wplauncher.Services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.InternalApps.Settings.OnChangeListener;
import com.robotjatek.wplauncher.Theme.DarkTheme;
import com.robotjatek.wplauncher.Theme.ITheme;
import com.robotjatek.wplauncher.Theme.LightTheme;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SettingsService {
    public static final String PREF_NAME = "WPLAUNCHER";
    public static final String SETTINGS = "SETTINGS";
    private boolean _disposed = false;
    private final List<OnChangeListener<AccentColor>> _accentChangeListeners = new ArrayList<>();
    private final List<Consumer<ITheme>> _themeChangeListeners = new ArrayList<>();
    private AccentColor _accentColor = Colors.ACCENT_COLORS.get(0);
    private final List<ITheme> _themes = List.of(new DarkTheme(), new LightTheme());
    private ITheme _theme = _themes.get(0);
    private final Context _context;

    public SettingsService(Context context) {
        _context = context;
        loadPersistedSettings();
    }

    @SuppressLint("ApplySharedPref")
    public void deleteSettings() {
        var prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }

    public AccentColor getAccentColor() {
        return _accentColor;
    }

    public void setAccentColor(AccentColor color) {
        _accentColor = color;
        _accentChangeListeners.forEach(l -> l.changed(color));
        persistSettings();
    }

    public void subscribeToAccentColorChange(OnChangeListener<AccentColor> listener) {
        _accentChangeListeners.add(listener);
    }

    public void unsubscribeFromAccentColorChange(OnChangeListener<AccentColor> listener) {
        _accentChangeListeners.remove(listener);
    }

    public void subscribeToThemeChange(Consumer<ITheme> listener) {
        _themeChangeListeners.add(listener);
    }

    public void unsubscribeFromThemeChange(Consumer<ITheme> listener) {
        _themeChangeListeners.remove(listener);
    }

    public List<ITheme> getThemes() {
        return _themes;
    }

    public void setCurrentTheme(ITheme theme) {
        _theme = theme;
        for (var listener : _themeChangeListeners) {
            listener.accept(theme);
        }
        persistSettings();
    }

    public ITheme getCurrentTheme() {
        return _theme;
    }

    private void persistSettings() {
        try {
            var settingsJson = new JSONObject();
            settingsJson.put("accentColor", _accentColor.color());
            settingsJson.put("theme", _theme.name());

            var prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(SETTINGS, settingsJson.toString())
                    .apply();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPersistedSettings() {
        var prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        var settingsJson = prefs.getString(SETTINGS, null);
        if (settingsJson == null) {
            // load default fallback values
            loadFallbackValues();
            return;
        }

        try {
            var settings = new JSONObject(settingsJson);

            var color = settings.getInt("accentColor");
            _accentColor = Colors.ACCENT_COLORS.stream()
                    .filter(c -> c.color() == color)
                    .findFirst()
                    .orElse(Colors.ACCENT_RED);
            var theme = settings.getString("theme");
            _theme = _themes.stream().filter(t -> t.name().equals(theme))
                    .findFirst()
                    .orElse(_themes.get(0));


        } catch (JSONException e) {
            loadFallbackValues();
            Log.e("SettingsService", "Failed to load persisted settings");
        }
    }

    private void loadFallbackValues() {
        _accentColor = Colors.ACCENT_RED;
        _theme = _themes.get(0);
    }

    public void dispose() {
        if (!_disposed) {
            persistSettings();
            _disposed = true;
        }
    }
}
