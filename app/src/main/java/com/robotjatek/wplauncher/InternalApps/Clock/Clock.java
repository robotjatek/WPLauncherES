package com.robotjatek.wplauncher.InternalApps.Clock;

import android.content.Context;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Checkbox.Checkbox;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Shader;

import org.json.JSONException;
import org.json.JSONObject;

// TODO: lots of duplicated boilerplate in internal apps
public class Clock implements IScreen {
    public static final String PREF_NAME = "WPLAUNCHER";
    public static final String SETTINGS = "CLOCK";

    private final IScreenNavigator _navigator;
    private final StackLayout _layout;

    private final Shader _shader = new Shader("","");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);
    private final Context _context;
    private boolean _locationEnabled;

    public Clock(IScreenNavigator navigator, Context context) {
        _navigator = navigator;
        _context = context;
        loadSettings();

        _layout = new StackLayout(_renderer);
        _layout.addChild(new Label("CLOCK", 52, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("Settings", 160, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Checkbox("Show location on tile when available", _locationEnabled, (b) -> {
            _locationEnabled = b;
            persistSettings();
        }, context));
    }

    private void persistSettings() {
        try {
            var settingJson = new JSONObject();
            settingJson.put("locationEnabled", _locationEnabled);

            var prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(SETTINGS, settingJson.toString())
                    .apply();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSettings() {
        var prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        var settingsJson = prefs.getString(SETTINGS, null);
        if (settingsJson == null) {
            _locationEnabled = false;
            return;
        }

        try {
            var settings = new JSONObject(settingsJson);
            _locationEnabled = settings.getBoolean("locationEnabled");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw(float delta, float[] projMatrix) {
        _layout.draw(delta, projMatrix);
    }

    @Override
    public void onBackPressed() {
        _navigator.pop();
    }

    @Override
    public void onResize(int width, int height) {
        _layout.onResize(width, height);
    }

    @Override
    public void onTouchStart(float x, float y) {
        _layout.onTouchStart(x, y);
    }

    @Override
    public void onTouchEnd(float x, float y) {
        _layout.onTouchEnd(x, y);
    }

    @Override
    public void onTouchMove(float x, float y) {
        _layout.onTouchMove(x, y);
    }

    @Override
    public void dispose() {
        persistSettings();
        _layout.dispose();
        _shader.delete();
        _renderer.dispose();
    }
}
