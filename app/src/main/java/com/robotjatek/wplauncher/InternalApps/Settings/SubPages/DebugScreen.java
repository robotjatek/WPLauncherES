package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Modal.Modal;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.MainActivity;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.TileGrid.Position;

// TODO: make this a generic pivot view with one tab
public class DebugScreen implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final SettingsService _settingsService;
    private final Context _context;
    private final StackLayout _layout = new StackLayout();
    private Size<Integer> _size = new Size<>(-1, -1);

    public DebugScreen(IScreenNavigator navigator, SettingsService settingsService, Context context) {
        _navigator = navigator;
        _settingsService = settingsService;
        _context = context;
        _layout.setBgColor(Colors.BLACK);
        // Title
        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("debug", 160, Typeface.NORMAL, Colors.WHITE, 0));

        // Content
        _layout.addChild(new Label("crash application", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                this::showCrashAppMessageBox));
        _layout.addChild(new Label("restart application", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                this::showRestartApplicationMessageBox));
        _layout.addChild(new Label("reset configuration", 96, Typeface.NORMAL, Colors.WHITE, 0, -1,
                this::showResetConfigurationMessageBox));
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

    private void showCrashAppMessageBox() {
        var modal = new Modal("WARNING!", "This will crash the application! A log will be created in the crash-log", () -> {
            throw new RuntimeException("App was crashed from the debug screen");
        }, _navigator::dismissModal);

        _navigator.openModal(modal);
    }

    private void showRestartApplicationMessageBox() {
        var modal = new Modal("WARNING!", "This will make the application restart",
                this::restartApplication, _navigator::dismissModal);
        _navigator.openModal(modal);
    }

    private void restartApplication() {
        var intent = new Intent(_context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    private void showResetConfigurationMessageBox() {
        var modal = new Modal("WARNING!", "This will delete the current configuration and restart the app",
                this::deleteConfiguration, _navigator::dismissModal);
        _navigator.openModal(modal);
    }

    private void deleteConfiguration() {
        _settingsService.deleteSettings();
        restartApplication();
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
