package com.robotjatek.wplauncher.InternalApps.Settings;

import android.content.Context;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.Spacer.Spacer;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.InternalApps.Settings.SubPages.PermissionsScreen;
import com.robotjatek.wplauncher.Services.PermissionService;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.InternalApps.Settings.SubPages.AboutScreen;
import com.robotjatek.wplauncher.InternalApps.Settings.SubPages.CrashLogScreen;
import com.robotjatek.wplauncher.InternalApps.Settings.SubPages.DebugScreen;
import com.robotjatek.wplauncher.InternalApps.Settings.SubPages.ThemeScreen;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.TileGrid.Position;

public class Settings implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final StackLayout _layout;
    private Size<Integer> _size = new Size<>(-1, -1);

    public Settings(IScreenNavigator navigator, SettingsService settings, PermissionService permissionService, Context context) {
        _navigator = navigator;
        _layout = new StackLayout();
        _layout.setBgColor(Colors.BLACK);
        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Spacer(0, 160));
        _layout.addChild(new Label("theme", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                () -> navigator.push(new ThemeScreen(navigator, settings))));
        _layout.addChild(new Label("permissions", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                () -> navigator.push(new PermissionsScreen(navigator, permissionService, context))));
        _layout.addChild(new Label("crash log", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                () -> navigator.push(new CrashLogScreen(navigator, context))));
        _layout.addChild(new Label("debug", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                () -> navigator.push(new DebugScreen(navigator))));
        _layout.addChild(new Label("about", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                () -> navigator.push(new AboutScreen(navigator, context, settings))));
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] view, QuadRenderer renderer) {
        _layout.draw(delta, projMatrix, view, renderer, Position.ZERO, _size);
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
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
