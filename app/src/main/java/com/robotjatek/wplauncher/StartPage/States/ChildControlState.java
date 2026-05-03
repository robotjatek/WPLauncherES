package com.robotjatek.wplauncher.StartPage.States;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.StartPage.StartScreen;

/**
 * {@link ChildControlState} relays all input to its children, without doing anything.
 * Stays in this state until the child releases its control
 */
public class ChildControlState extends BaseState {

    public ChildControlState(StartScreen context) {
        super(context);
    }


    @Override
    public boolean handleGesture(Gesture gesture) {
        return _context.getCurrentPage().handleGesture(gesture);
    }

    @Override
    public void update(float delta) {
        if (!_context.isChildrenCatchingGestures()) {
            _context.changeState(_context.IDLE_STATE());
        }
    }
}
