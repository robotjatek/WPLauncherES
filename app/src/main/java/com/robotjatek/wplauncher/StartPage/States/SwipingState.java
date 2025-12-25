package com.robotjatek.wplauncher.StartPage.States;

import com.robotjatek.wplauncher.StartPage.StartScreen;

/**
 * Updates page offset while moving, changes page on touch end if a threshold is reached
 * Moves to {@link IdleState} on touch end
 */
public class SwipingState extends BaseState {
    private float _lastX;

    public SwipingState(StartScreen context, float initialX) {
        super(context);
        _lastX = initialX;
    }

    @Override
    public void handleTouchStart(float x, float y) {
    }

    @Override
    public void handleMove(float x, float y) {
        var dx = x - _lastX;
        _lastX = x;
        _context.setPageOffset(_context.getPageOffset() + dx);
        _context.getCurrentPage().touchMove(x, y);
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        var threshold = _context.getScreenWidth() / 10f;
        if (_context.getPageOffset() > threshold) {
            _context.previousPage();
        }
        else if (_context.getPageOffset() < -threshold) {
            _context.nextPage();
        }
        _context.setPageOffset(0);
        _context.changeState(_context.IDLE_STATE());
    }
}
