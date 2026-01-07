package com.robotjatek.wplauncher.InternalApps.Settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.InternalApps.Settings.SubPages.ColorPickerScreen;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.AccentColor;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.Shader;

/**
 * Note Settings page should be completely STATELESS as its a singleton!
 */
public class Settings implements IScreen, OnChangeListener<AccentColor> {

    private final IScreenNavigator _navigator;
    private final Context _context;
    private final StackLayout _layout;
    private final Shader _shader = new Shader("","");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);
    private final SettingsService _settings;
    private final ColorPickerScreen _colorPickerScreen;
    private final Button _colorPickerBtn;
    private Bitmap _icon;

    public Settings(IScreenNavigator navigator, SettingsService settings, Context context) {
        _navigator = navigator;
        _settings = settings;
        _context = context;
        _layout = new StackLayout(_renderer);
        _layout.addChild(new Label("LAUNCHER SETTINGS", 52, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("theme", 160, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("Accent color", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0));

        _colorPickerScreen = new ColorPickerScreen(_renderer, navigator);
        _colorPickerScreen.subscribe(this);

        var color = settings.getAccentColor();
        _icon = BitmapUtil.createRect(64, 64, 8, color.color());
        _colorPickerBtn = new Button(color.name(), new BitmapDrawable(context.getResources(), _icon),
                () -> navigator.push(_colorPickerScreen));
        _layout.addChild(_colorPickerBtn);

        // TODO: Light/Dark mode selector
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
    public void dispose() {
        _renderer.dispose();
        _shader.delete();
        _layout.dispose();
        _colorPickerScreen.dispose();
        _icon.recycle();
    }

    @Override
    public void changed(AccentColor changed) {
        _settings.setAccentColor(changed);
        _icon.recycle();
        _colorPickerBtn.setText(changed.name());
        _icon = BitmapUtil.createRect(64, 64, 8, changed.color());
        _colorPickerBtn.setIcon(new BitmapDrawable(_context.getResources(), _icon));
    }
}
