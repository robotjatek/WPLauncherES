package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.InternalApps.Settings.OnChangeListener;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.AccentColor;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.TileGrid.Position;

public class ThemeScreen implements IScreen, OnChangeListener<AccentColor> {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final StackLayout _layout;
    private final Button _colorPickerBtn;
    private Icon _icon;
    private final SettingsService _settings;
    private final float[] _view = new float[16];
    private Size<Integer> _size = new Size<>(-1, -1);

    public ThemeScreen(IScreenNavigator navigator, SettingsService settings) {
        _navigator = navigator;
        _settings = settings;
        _layout = new StackLayout();
        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("theme", 160, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("Accent color", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0));

        var color = settings.getAccentColor();
        _icon = new Icon(color.color(), new Size<>(64, 64));
        _colorPickerBtn = new Button(
                color.name(),
                _icon,
                new Size<>(0, 100),
                () -> {
                    var colorPickerScreen = new ColorPickerScreen(navigator);
                    colorPickerScreen.subscribe(this);
                    navigator.push(colorPickerScreen);
                });
        _layout.addChild(_colorPickerBtn);

        // TODO: Light/Dark mode selector
    }

    @Override
    public void draw(float delta, float[] projMatrix, QuadRenderer renderer) {
        Matrix.setIdentityM(_view, 0);
        _layout.draw(delta, projMatrix, _view, renderer, Position.ZERO, _size);
    }

    @Override
    public void onBackPressed() {
        _navigator.pop();
    }

    @Override
    public void onResize(int width, int height) {
        _size = new Size<>(width, height);
        _layout.onResize(width, height);
    }

    @Override
    public void changed(AccentColor changed) {
        _settings.setAccentColor(changed);
        _icon.dispose();
        _colorPickerBtn.setText(changed.name());
        _icon = new Icon(changed.color(), new Size<>(64, 64));
        _colorPickerBtn.setIcon(_icon);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _icon.dispose();
            _disposed = true;
        }
    }
}
