package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
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
    public boolean handleLongPress(LongPressGesture gesture) {
        _context.changeState(_context.EDIT_STATE(gesture.getX(), gesture.getY()));
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        return true; // TODO: itt ez nem lesz jó szerintem, scroll lesz ez, és scrollstatebe change
    }

    @Override
    public void enter() {
        super.enter();
        _context.cancelSelection();
    }
}
