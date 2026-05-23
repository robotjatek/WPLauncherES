package com.robotjatek.wplauncher.StartScreen.States;

import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.StartScreen.StartScreen;

/**
 * Updates page offset while moving, changes page on touch end if a threshold is reached
 * Moves to {@link SnapState} on touch end
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
    public boolean handleMove(MoveGesture gesture) {
        var dx = gesture.getX() - _lastX;
        _lastX = gesture.getX();

        _context.setPageOffset(_context.getPageOffset() + dx);
        return true;
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
        var currentOffset = _context.getPageOffset();

        if (currentOffset > threshold) {
            _context.previousPage();
            _context.setPageOffset(currentOffset - _context.getScreenWidth());
        } else if (currentOffset < -threshold) {
            _context.nextPage();
            _context.setPageOffset(currentOffset + _context.getScreenWidth());
        }

        _context.changeState(_context.SNAP_STATE());
        return true;
    }
}
