package com.robotjatek.wplauncher.TileGrid.States;

import android.view.ViewConfiguration;

import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class TouchingState extends BaseState {

    private final float _touchStartX;
    private final float _touchStartY;
    private long _touchStartTime = -1;

    public TouchingState(TileGrid context, float x, float y) {
        super(context);
        _touchStartX = x;
        _touchStartY = y;
    }

    @Override
    public void enter() {
        super.enter();
        _touchStartTime = System.currentTimeMillis();
    }

    @Override
    public void update(float delta) {
        var elapsedTime = System.currentTimeMillis() - _touchStartTime;
        if (elapsedTime > ViewConfiguration.getLongPressTimeout()) {
            _context.changeState(_context.EDIT_STATE(_touchStartX, _touchStartY));
        }
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        // Quick tap, touch was shorter than the long-press timeout
        _context.changeState(_context.TAPPED_STATE(_touchStartX, _touchStartY));
    }

    @Override
    public void handleMove(float x, float y) {
        var deltaX = x - _touchStartX;
        var deltaY = y - _touchStartY;

        if (Math.abs(deltaY) > 10 && Math.abs(deltaY) > Math.abs(deltaX)) {
            _context.changeState(_context.SCROLL_STATE(y));
        } else if (Math.abs(deltaX) > 0 || Math.abs(deltaY) > 0) {
            _touchStartTime = System.currentTimeMillis();
            _context.changeState(_context.IDLE_STATE());
        }
    }
}
