package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class IdleState extends BaseState {

    public IdleState(TileGrid context) {
        super(context);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        _context.changeState(_context.TAPPED_STATE(gesture.getX(), gesture.getY()));
        return true;
    }

    @Override
    public void enter() {
        super.enter();
        _context.cancelSelection();
    }
}
