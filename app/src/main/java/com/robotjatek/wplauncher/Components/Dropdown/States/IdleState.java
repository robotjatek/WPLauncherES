package com.robotjatek.wplauncher.Components.Dropdown.States;

import com.robotjatek.wplauncher.Components.Dropdown.Dropdown;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.IState;

public class IdleState<T> implements IState {

    private final Dropdown<T> _context;

    public IdleState(Dropdown<T> context) {
        _context = context;
    }

    @Override
    public void enter() {}

    @Override
    public void exit() {}

    @Override
    public void update(float delta) {
        _context.getTouchhandler().update(delta);
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        if (_context.iClosed()) {
            _context.getTouchhandler().onDown(gesture.getX(), gesture.getY());
        } else {
            _context.getContentLayout().handleGesture(gesture);
        }
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _context.getTouchhandler().onMove(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        if (_context.iClosed()) {
            _context.getTouchhandler().onUp();
        } else {
            _context.getContentLayout().handleGesture(gesture);
        }
        return true;
    }
}
