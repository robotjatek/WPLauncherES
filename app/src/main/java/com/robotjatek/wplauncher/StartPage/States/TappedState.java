package com.robotjatek.wplauncher.StartPage.States;

import android.util.Log;

import com.robotjatek.wplauncher.IGestureState;
import com.robotjatek.wplauncher.StartPage.StartScreen;

public class TappedState extends BaseState {

    private final float _x;
    private final float _y;

    public TappedState(StartScreen context, float x, float y) {
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
        Log.w(IGestureState.class.toString(), "Touch end shouldn't happen here");
    }

    @Override
    public void enter() {
        super.enter();
        // Tell child that a tap happened, then go back to idle
        _context.getCurrentPage().touchEnd(_x, _y); // TODO: should make this a tapEvent() call
        _context.changeState(_context.IDLE_STATE());
    }
}
