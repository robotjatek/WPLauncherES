package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class ScrollState extends BaseState {
    private final float _startY;

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
    public void handleTouchEnd(float x, float y) {
        _context.getScroll().onTouchEnd();
        _context.changeState(_context.IDLE_STATE());
    }

    @Override
    public void handleMove(float x, float y) {
        _context.getScroll().onTouchMove(y);
    }
}
