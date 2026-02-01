package com.robotjatek.wplauncher.InternalApps.Notifications;

import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;

public class Notifications implements IScreen {

    private final IScreenNavigator _navigator;

    public Notifications(IScreenNavigator navigator) {
        _navigator = navigator;
    }

    @Override
    public void draw(float delta, float[] projMatrix, QuadRenderer renderer) {

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

    }
}
