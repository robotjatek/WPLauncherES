package com.robotjatek.wplauncher.Services.ScreenNavigator.States;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.Services.ScreenNavigator.ScreenNavigator;

public abstract class BaseState implements IState {

    protected final ScreenNavigator _context;

    protected BaseState(ScreenNavigator context) {
        _context = context;
    }

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        var modal = _context.getModal();
        var modalTop = -LauncherRenderer.SCREEN_DATA.topInset;
        var modalHeight = _context.getHeight() / 3f;
        var modalBottom = modalTop + modalHeight;

        if (modal != null) {
            if (gesture.getY() >= modalTop && gesture.getY() <= modalBottom) {
                return modal.handleGesture(gesture);
            } else {
                _context.dismissModal();
                return true;
            }
        }

        return _context.getCurrentScreen().handleGesture(gesture);
    }
}
