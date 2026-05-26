package com.robotjatek.wplauncher.Components.Button.States;

import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.Gestures.DownGesture;

public class IdleState extends ButtonBaseState {
    public IdleState(Button context) {
        super(context);
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        _context.changeState(_context.PRESSED_STATE(gesture.getX(), gesture.getY()));
        return true;
    }
}
