package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.content.Context;
import android.content.Intent;

import com.robotjatek.wplauncher.Components.Checkbox.Checkbox;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Modal.Modal;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.Spacer.Spacer;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.MainActivity;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.Theme.ITheme;
import com.robotjatek.wplauncher.TileGrid.Position;

// TODO: make this a generic pivot view with one tab
public class DebugScreen implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final SettingsService _settingsService;
    private final Context _context;
    private final StackLayout _layout;
    private Size<Integer> _size = new Size<>(-1, -1);
    private final Checkbox _uncapCheckbox;

    public DebugScreen(IScreenNavigator navigator, SettingsService settingsService, Context context) {
        _navigator = navigator;
        _settingsService = settingsService;
        _context = context;
        _layout = new StackLayout(() -> settingsService.getCurrentTheme().layout());
        // Title
        _layout.addChild(new Label("LAUNCHER SETTINGS", () -> settingsService.getCurrentTheme().label(ITheme.TextRole.TITLE)));
        _layout.addChild(new Label("debug", () -> settingsService.getCurrentTheme().label(ITheme.TextRole.SUBTITLE)));

        // Content
        _layout.addChild(new Label("crash application", () -> settingsService.getCurrentTheme().label(ITheme.TextRole.PAGE_LINK), -1,
                this::showCrashAppMessageBox));
        _layout.addChild(new Label("restart application", () -> settingsService.getCurrentTheme().label(ITheme.TextRole.PAGE_LINK), -1,
                this::showRestartApplicationMessageBox));
        _layout.addChild(new Label("reset configuration", () -> settingsService.getCurrentTheme().label(ITheme.TextRole.PAGE_LINK), -1,
                this::showResetConfigurationMessageBox));

        _layout.addChild(new Spacer(0, 128));
        _layout.addChild(
                new Checkbox(
                        "Show FPS counter",
                        _settingsService.fpsCounter(),
                        _settingsService::fpsCounter,
                        context,
                        () -> settingsService.getCurrentTheme().checkbox()));

        _layout.addChild(new Spacer(0, 16));
        _uncapCheckbox = new Checkbox(
                "Uncap frame rate",
                _settingsService.uncapFrameRate(),
                this::openUncapWarningModal,
                context,
                () -> settingsService.getCurrentTheme().checkbox());
        _layout.addChild(_uncapCheckbox);
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
        }, _navigator::dismissModal, () -> _settingsService.getCurrentTheme().modal());

        _navigator.openModal(modal);
    }

    private void showRestartApplicationMessageBox() {
        var modal = new Modal("WARNING!", "This will make the application restart",
                this::restartApplication, _navigator::dismissModal, () -> _settingsService.getCurrentTheme().modal());
        _navigator.openModal(modal);
    }

    private void restartApplication() {
        var intent = new Intent(_context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(intent);
    }

    private void showResetConfigurationMessageBox() {
        var modal = new Modal("WARNING!", "This will delete the current configuration and restart the app",
                this::deleteConfiguration, _navigator::dismissModal, () -> _settingsService.getCurrentTheme().modal());
        _navigator.openModal(modal);
    }

    private void deleteConfiguration() {
        _settingsService.deleteSettings();
        restartApplication();
    }

    private void openUncapWarningModal(boolean enabled) {
        Modal modal;
        if (enabled) {
            modal = new Modal(
                    "WARNING!",
                    "This will uncap the frame rate, which could affect battery-life negatively." +
                            " To re-enable the frame rate cap you'll need to restart the app.",
                    () -> {
                        // OK
                        _settingsService.uncapFrameRate(true);
                        _navigator.dismissModal();
                    },
                    () -> {
                        // Cancel
                        _navigator.dismissModal();
                        _uncapCheckbox.setState(false);
                    },
                    () -> _settingsService.getCurrentTheme().modal());
        } else {
            modal = new Modal(
                    "WARNING!",
                    "The application will restart.",
                    () -> {
                        // OK
                        _settingsService.uncapFrameRate(false);
                        _navigator.dismissModal();
                        restartApplication();
                    },
                    () -> {
                        // Cancel
                        _navigator.dismissModal();
                        _uncapCheckbox.setState(true);
                    },
                    () -> _settingsService.getCurrentTheme().modal());
        }
        _navigator.openModal(modal);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
