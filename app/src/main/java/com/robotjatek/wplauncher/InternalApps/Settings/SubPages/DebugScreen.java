package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Modal.Modal;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.Spacer.Spacer;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

// TODO: make this a generic pivot view with one tab
public class DebugScreen implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final StackLayout _layout = new StackLayout();
    private Size<Integer> _size = new Size<>(-1, -1);
    private final float[] _view = new float[16];

    public DebugScreen(IScreenNavigator navigator) {
        _navigator = navigator;
        // Title
        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("debug", 160, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Spacer(0, 48));

        // Content
        _layout.addChild(new Label("crash application", 72, Typeface.NORMAL, Colors.WHITE, 0, -1,
                this::showCrashAppMessageBox));
        _layout.addChild(new Label("reset StartPage state-machine", 72, Typeface.NORMAL, Colors.WHITE, 0, -1,
                this::resetStateMachine));
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
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    private void showCrashAppMessageBox() {
        var modal = new Modal("WARNING!", "This will crash the application! A log will be created in the crash-log", () -> {
            throw new RuntimeException("App was crashed from the debug screen");
        }, _navigator::dismissModal);

        _navigator.openModal(modal);
    }

    private void resetStateMachine() {
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
