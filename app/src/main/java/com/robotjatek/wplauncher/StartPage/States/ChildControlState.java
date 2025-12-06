package com.robotjatek.wplauncher.StartPage.States;

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
    public void handleTouchStart(float x, float y) {
        _context.getCurrentPage().touchStart(x, y);
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        _context.getCurrentPage().touchEnd(x, y);
    }

    @Override
    public void handleMove(float x, float y) {
        _context.getCurrentPage().touchMove(x, y);
    }

    @Override
    public void update(float delta) {
        if (!_context.isChildrenCatchingGestures()) {
            _context.changeState(_context.IDLE_STATE());
        }
    }
}
