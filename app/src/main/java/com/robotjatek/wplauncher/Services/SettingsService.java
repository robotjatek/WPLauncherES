package com.robotjatek.wplauncher.Services;

import com.robotjatek.wplauncher.Colors;

// TODO: persist settings
// TODO: emit setting change event or tilepage
public class SettingsService {
    private AccentColor _accentColor = Colors.ACCENT_COLORS.get(0);

    public AccentColor getAccentColor() {
        return _accentColor;
    }

    public void setAccentColor(AccentColor color) {
        _accentColor = color;
    }

    public void dispose() {
    }
}
