package com.robotjatek.wplauncher.StartPage.States;

import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
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
    public void enter() {
        super.enter();
        _context.getCurrentPage().resetState();
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        var dx = gesture.getX() - _lastX;
        _lastX = gesture.getX();

        _context.setPageOffset(_context.getPageOffset() + dx);
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        var threshold = _context.getScreenWidth() / 10f;
        if (_context.getPageOffset() > threshold) {
            _context.previousPage();
        } else if (_context.getPageOffset() < -threshold) {
            _context.nextPage();
        }

        _context.setPageOffset(0);
        _context.changeState(_context.IDLE_STATE());
        return true;
    }
}
