package com.robotjatek.wplauncher.Services;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.InternalApps.Settings.OnChangeListener;

import java.util.ArrayList;
import java.util.List;

// TODO: persist settings
public class SettingsService {
    private List<OnChangeListener<AccentColor>> _listeners = new ArrayList<>();
    private AccentColor _accentColor = Colors.ACCENT_COLORS.get(0);

    public AccentColor getAccentColor() {
        return _accentColor;
    }

    public void setAccentColor(AccentColor color) {
        _accentColor = color;
        _listeners.forEach(l -> l.changed(color));
    }

    public void subscribe(OnChangeListener<AccentColor> listener) {
        _listeners.add(listener);
    }

    public void dispose() {
    }
}
