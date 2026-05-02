package com.robotjatek.wplauncher.Components.ListPage.States;

import com.robotjatek.wplauncher.Components.ListPage.ListPage;

public class ScrollState<T> extends BaseState<T> {
    private final float _startY;
    private boolean _touching = true;

    public ScrollState(ListPage<T> context, float y) {
        super(context);
        _startY = y;
    }

    @Override
    public void enter() {
        super.enter();
        _context.getScroll().onTouchStart(_startY);
    }

    @Override
    public void handleTouchStart(float x, float y) {
        // handle retap on flinging
        _touching = true;
        _context.getScroll().onTouchStart(y);
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        _context.getScroll().onTouchEnd();
        _touching = false;
    }

    @Override
    public void handleMove(float x, float y) {
        _context.getScroll().onTouchMove(y);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (!_touching && !_context.getScroll().isFlinging()) {
            _context.changeState(_context.IDLE_STATE());
        }
    }
}
