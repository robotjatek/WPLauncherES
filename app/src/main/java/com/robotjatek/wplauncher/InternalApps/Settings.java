package com.robotjatek.wplauncher.InternalApps;

import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Shader;

public class Settings implements IScreen {

    private final IScreenNavigator _navigator;
    private final Shader _shader = new Shader("","");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);

    public Settings(IScreenNavigator navigator) {
        _navigator = navigator;
    }

    @Override
    public void draw(float delta, float[] projMatrix) {
    }

    @Override
    public void onBackPressed() {
        _navigator.pop();
    }

    @Override
    public void onResize(int width, int height) {

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
