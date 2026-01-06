package com.robotjatek.wplauncher.InternalApps.Components.List.States;

import com.robotjatek.wplauncher.InternalApps.Components.List.ListView;

public class ScrollState<T> extends BaseState<T> {
    private final float _startY;

    public ScrollState(ListView<T> context, float y) {
        super(context);
        _startY = y;
    }

    @Override
    public void enter() {
        super.enter();
        _context.getScroll().onTouchStart(_startY);
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        _context.getScroll().onTouchEnd();
        _context.changeState(_context.IDLE_STATE()); // TODO: low prio fix: remain in scroll state while flinging
    }

    @Override
    public void handleMove(float x, float y) {
        _context.getScroll().onTouchMove(y);
    }
}
