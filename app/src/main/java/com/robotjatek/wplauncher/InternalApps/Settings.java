package com.robotjatek.wplauncher.InternalApps;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.InternalApps.Components.Label.Label;
import com.robotjatek.wplauncher.InternalApps.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Shader;

/**
 * TODO: settings page should be completely STATELESS as its a singleton!
 */
public class Settings implements IScreen {

    private final IScreenNavigator _navigator;
    private final StackLayout _layout;
    private final Shader _shader = new Shader("","");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);

    public Settings(IScreenNavigator navigator) {
        _navigator = navigator;
        _layout = new StackLayout(_renderer);

        _layout.addChild(new Label("LAUNCHER SETTINGS", 52, Typeface.NORMAL, 0xffffffff, 0));
        _layout.addChild(new Label("theme", 160, Typeface.NORMAL, 0xffffffff, 0));
        _layout.addChild(new Label("Harmadik meg bold", 78, Typeface.BOLD, 0xffffffff, 0xffff0000));

        // TODO: Background color dropdown
        // TODO: Accent color dropdown
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

    }

    @Override
    public void onTouchEnd(float x, float y) {

    }

    @Override
    public void onTouchMove(float x, float y) {

    }

    @Override
    public void dispose() {
        _renderer.dispose();
        _shader.delete();
    }
}
