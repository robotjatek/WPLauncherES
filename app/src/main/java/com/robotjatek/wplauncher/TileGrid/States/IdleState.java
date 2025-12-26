package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class IdleState extends BaseState {

    public IdleState(TileGrid context) {
        super(context);
    }

    @Override
    public void enter() {
        _context.cancelSelection();
    }

    @Override
    public void handleTouchStart(float x, float y) {
        _context.changeState(_context.TOUCHING_STATE(x, y));
    }
}
