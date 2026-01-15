package com.robotjatek.wplauncher.InternalApps.Clock;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Shader;

// TODO: lots of duplicated boilerplate in internal apps
public class Clock implements IScreen {

    private final IScreenNavigator _navigator;
    private final StackLayout _layout;

    private final Shader _shader = new Shader("","");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);

    public Clock(IScreenNavigator navigator) {
        _navigator = navigator;
        _layout = new StackLayout(_renderer);
        _layout.addChild(new Label("CLOCK", 52, Typeface.NORMAL, Colors.WHITE, 0));
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
        _layout.dispose();
        _shader.delete();
        _renderer.dispose();
    }
}
