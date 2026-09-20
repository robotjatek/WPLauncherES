package com.robotjatek.wplauncher.Components.Dropdown.States;

import com.robotjatek.wplauncher.Components.Dropdown.Dropdown;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.IState;

public class IdleState implements IState {

    private final Dropdown _context;

    public IdleState(Dropdown context) {
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
        _context.getTouchhandler().onDown(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _context.getTouchhandler().onMove(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _context.getTouchhandler().onUp();
        return true;
    }
}
