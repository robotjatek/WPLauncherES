package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public class CrashLogScreen implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final StackLayout _layout = new StackLayout();
    private final Size<Integer> _size = new Size<>(-1, -1);
    private final float[] _view = new float[16];

    public CrashLogScreen(IScreenNavigator navigator) {
        _navigator = navigator;
        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("crash log", 160, Typeface.NORMAL, Colors.WHITE, 0));
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
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
