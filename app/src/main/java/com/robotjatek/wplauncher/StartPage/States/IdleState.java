package com.robotjatek.wplauncher.StartPage.States;

import com.robotjatek.wplauncher.StartPage.StartScreen;

public class IdleState extends BaseState {

    public IdleState(StartScreen context) {
        super(context);
    }

    @Override
    public void handleTouchStart(float x, float y) {
        _context.changeState(_context.TOUCHING_STATE(x, y));
    }

    @Override
    public void handleTouchEnd(float x, float y) {}

    @Override
    public void handleMove(float x, float y) {}
}
