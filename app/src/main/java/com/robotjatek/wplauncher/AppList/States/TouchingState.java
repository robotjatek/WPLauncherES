package com.robotjatek.wplauncher.AppList.States;

import android.view.ViewConfiguration;

import com.robotjatek.wplauncher.AppList.AppList;

/**
 * Measures touch time
 * Transitions to {@link ScrollState} on vertical move
 * Transitions to {@link ContextMenuState} after enough time has passed to do a long-press gesture
 * Transitions to {@link TappedState} if the finger was released before the long-press timeout
 * Transitions to {@link IdleState} if the finger starts moving while trying to measure hold time
 */
public class TouchingState extends BaseState {

    private final float _touchStartX;
    private final float _touchStartY;
    private long _touchStartTime = -1;

    public TouchingState(AppList context, float x, float y) {
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
        // measure hold time, if it exceeds the LongPressTimeout move into ContextMenu state
        var deltaTime = System.currentTimeMillis() - _touchStartTime;
        if (deltaTime > ViewConfiguration.getLongPressTimeout()) {
            _context.changeState(_context.CONTEXT_MENU_STATE(_touchStartX, _touchStartY));
        }
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        // This was a quick tap, shorter than the long-press timeout
        _context.changeState(_context.TAPPED_STATE(_touchStartY));
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
