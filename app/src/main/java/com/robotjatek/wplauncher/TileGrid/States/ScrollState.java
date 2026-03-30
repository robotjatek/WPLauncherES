package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class ScrollState extends BaseState {
    private final float _startY;
    private boolean _touching = true;

    public ScrollState(TileGrid context, float y) {
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
        _touching = false;
        _context.getScroll().onTouchEnd();
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
