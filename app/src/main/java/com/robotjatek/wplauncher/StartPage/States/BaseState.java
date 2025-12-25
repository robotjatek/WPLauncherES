package com.robotjatek.wplauncher.StartPage.States;

import android.util.Log;

import com.robotjatek.wplauncher.IGestureState;
import com.robotjatek.wplauncher.StartPage.StartScreen;

public abstract class BaseState implements IGestureState {

    protected final StartScreen _context;

    protected BaseState(StartScreen context) {
        _context = context;
    }

    @Override
    public void enter() {
        Log.d(IGestureState.class.toString(), this.getClass().toString());
    }

    @Override
    public void exit() {
    }

    @Override
    public void update(float delta) {
        // safe-guard to automatically go to CHILD_CONTROL_STATE whenever a child assumes control
        if (_context.isChildrenCatchingGestures()) {
            _context.changeState(_context.CHILD_CONTROL_STATE());
        }
    }
}
