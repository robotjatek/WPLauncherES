package com.robotjatek.wplauncher.Components.InputBox.States;

import com.robotjatek.wplauncher.Components.InputBox.InputBox;
import com.robotjatek.wplauncher.Gestures.TapGesture;

/**
 * Idle state of the input box. In this state the box is unfocused
 * On tap it transitions to Active state which handles the input
 */
public class IdleState extends BaseState {

    public IdleState(InputBox _context) {
        super(_context);
    }

    @Override
    public void enter() {
        super.enter();
        _context.showCursor(false);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        _context.setCursorPositionWithXPosition(gesture.getX());
        _context.changeState(_context.ACTIVE_STATE(gesture.getUIContext()));
        return true;
    }
}
