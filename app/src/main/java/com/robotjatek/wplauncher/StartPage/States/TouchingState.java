package com.robotjatek.wplauncher.StartPage.States;

import android.util.Log;
import android.view.ViewConfiguration;

import com.robotjatek.wplauncher.IGestureState;
import com.robotjatek.wplauncher.StartPage.StartScreen;

public class TouchingState extends BaseState {

    private long _touchStartTime = 0;
    private final float _touchStartX;
    private final float _touchStartY;

    public TouchingState(StartScreen context, float x, float y) {
        super(context);
        _touchStartX = x;
        _touchStartY = y;
    }

    @Override
    public void enter() {
        _context.getCurrentPage().touchStart(_touchStartX, _touchStartY);
        _touchStartTime = System.currentTimeMillis();
    }

    @Override
    public void handleTouchStart(float x, float y) {
        // NO-OP: already in touch state
        Log.w(IGestureState.class.toString(), "Already in touch state, this shouldn't happen");
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        // quick tap => tapped state
        _context.changeState(_context.TAPPED_STATE(x, y));
    }

    @Override
    public void handleMove(float x, float y) {
        if (_context.isChildrenCatchingGestures()) {
            _context.changeState(_context.CHILD_CONTROL_STATE());
            return;
        }

        var _totalDeltaX = x - _touchStartX;
        var _totalDeltaY = y - _touchStartY;

        if (Math.abs(_totalDeltaX) > 30 && Math.abs(_totalDeltaX) > Math.abs(_totalDeltaY)) {
            _context.changeState(_context.SWIPE_STATE(x));
        } else if (Math.abs(_totalDeltaY) > 10 && Math.abs(_totalDeltaY) > Math.abs(_totalDeltaX)) {
            _context.changeState(_context.SCROLL_STATE());
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        // Transition to long tap if enough time has passed
        var deltaTime = System.currentTimeMillis() - _touchStartTime;
        if (deltaTime > ViewConfiguration.getLongPressTimeout()) {
            _context.changeState(_context.LONG_PRESS_STATE(_touchStartX, _touchStartY));
        }
    }

}
