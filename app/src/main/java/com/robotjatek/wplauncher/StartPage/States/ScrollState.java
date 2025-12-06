package com.robotjatek.wplauncher.StartPage.States;

import com.robotjatek.wplauncher.StartPage.StartScreen;

/**
 * {@link ScrollState} Relays move intent to the children and disregards any other input
 * Transitions to {@link IdleState} when the move ends
 */
public class ScrollState extends BaseState {

    public ScrollState(StartScreen context) {
        super(context);
    }

    @Override
    public void handleTouchStart(float x, float y) {

    }

    @Override
    public void handleTouchEnd(float x, float y) {
        _context.changeState(_context.IDLE_STATE());
    }

    @Override
    public void handleMove(float x, float y) {
        _context.getCurrentPage().touchMove(x, y);
    }
}
