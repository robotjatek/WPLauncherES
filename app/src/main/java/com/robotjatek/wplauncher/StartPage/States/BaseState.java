package com.robotjatek.wplauncher.StartPage.States;

import android.util.Log;

import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.StartPage.StartScreen;

public abstract class BaseState implements IState {

    protected final StartScreen _context;

    protected BaseState(StartScreen context) {
        _context = context;
    }

    @Override
    public void enter() {
        Log.d(IState.class.toString(), this.getClass().toString());
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
