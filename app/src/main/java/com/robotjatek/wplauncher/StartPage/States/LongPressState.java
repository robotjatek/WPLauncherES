package com.robotjatek.wplauncher.StartPage.States;

import android.util.Log;

import com.robotjatek.wplauncher.StartPage.StartScreen;

public class LongPressState extends BaseState {

    private final float _x;
    private final float _y;

    public LongPressState(StartScreen context, float x, float y) {
        super(context);
        _x = x;
        _y = y;
    }

    @Override
    public void handleTouchStart(float x, float y) {
        Log.w(IGestureState.class.toString(), "Touch start shouldn't happen here");
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        Log.w(IGestureState.class.toString(), "Touch end shouldn't happen here");
    }

    @Override
    public void handleMove(float x, float y) {
        Log.w(IGestureState.class.toString(), "Move shouldn't happen here");
    }

    @Override
    public void enter() {
        super.enter();
        // Tell child that a long press happened and change to child control state
        _context.getCurrentPage().handleLongPress(_x, _y);
        _context.changeState(_context.CHILD_CONTROL_STATE());
    }
}
