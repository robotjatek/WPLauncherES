package com.robotjatek.wplauncher.InternalApps.Settings;

import android.content.Context;
import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.InternalApps.Settings.SubPages.CrashLogScreen;
import com.robotjatek.wplauncher.InternalApps.Settings.SubPages.ThemeScreen;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.TileGrid.Position;

/**
 * Note: Settings page should be completely STATELESS as it's a singleton!
 */
public class Settings implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final StackLayout _layout;
    private Size<Integer> _size = new Size<>(-1, -1);
    private final float[] _view = new float[16];
    private final ThemeScreen _themeScreen;
    private final CrashLogScreen _crashScreen;

    public Settings(IScreenNavigator navigator, SettingsService settings, Context context) {
        _navigator = navigator;
        _themeScreen = new ThemeScreen(navigator, settings, context);
        _crashScreen = new CrashLogScreen(navigator);
        _layout = new StackLayout();
        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));

        _layout.addChild(new Label("", 160, Typeface.NORMAL, Colors.WHITE, 0, -1, null)); // TODO: proper spacer (dedicated element, or an empty layout?)
        _layout.addChild(new Label("theme", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                () -> navigator.push(_themeScreen)));
        _layout.addChild(new Label("crash log", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                () -> navigator.push(_crashScreen)));
        _layout.addChild(new Label("about", 96, Typeface.NORMAL, Colors.WHITE, 0, -1, null));
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
        _themeScreen.onResize(width, height); // TODO: calling onResize manually for children is very error prone
        _crashScreen.onResize(width, height);
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
        if (!_disposed) {
            _layout.dispose();
            _themeScreen.dispose();
            _crashScreen.dispose();
            _disposed = true;
        }
    }
}
