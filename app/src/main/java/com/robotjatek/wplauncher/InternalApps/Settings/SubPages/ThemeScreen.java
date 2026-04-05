package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.InternalApps.Settings.OnChangeListener;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.AccentColor;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.TileGrid.Position;

public class ThemeScreen implements IScreen, OnChangeListener<AccentColor> {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final StackLayout _layout;
    private final ColorPickerScreen _colorPickerScreen;
    private final Button _colorPickerBtn;
    private Bitmap _icon;
    private final Context _context;
    private final SettingsService _settings;
    private final float[] _view = new float[16];
    private Size<Integer> _size = new Size<>(-1, -1);

    public ThemeScreen(IScreenNavigator navigator, SettingsService settings, Context context) {
        _navigator = navigator;
        _context = context;
        _settings = settings;
        _layout = new StackLayout();
        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("theme", 160, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("Accent color", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0));

        _colorPickerScreen = new ColorPickerScreen(navigator);
        _colorPickerScreen.subscribe(this);

        var color = settings.getAccentColor();
        _icon = BitmapUtil.createRect(64, 64, 8, color.color());
        _colorPickerBtn = new Button(color.name(), new BitmapDrawable(context.getResources(), _icon),
                () -> navigator.push(_colorPickerScreen));
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
        _colorPickerScreen.onResize(width, height);
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
    public void changed(AccentColor changed) {
        _settings.setAccentColor(changed);
        _icon.recycle();
        _colorPickerBtn.setText(changed.name());
        _icon = BitmapUtil.createRect(64, 64, 8, changed.color()); // TODO: real icon component?
        _colorPickerBtn.setIcon(new BitmapDrawable(_context.getResources(), _icon));
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _colorPickerScreen.dispose();
            _icon.recycle();
            _disposed = true;
        }
    }
}
