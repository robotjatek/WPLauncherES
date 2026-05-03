package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class IdleState extends BaseState {

    public IdleState(TileGrid context) {
        super(context);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        var tappedTile = getTileAt(gesture.getX(), gesture.getY());
        tappedTile.ifPresent(Tile::onTap);
        return true;
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        _context.changeState(_context.EDIT_STATE(gesture.getX(), gesture.getY()));
        return true;
    }


    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture);
    }

    @Override
    public void enter() {
        super.enter();
        _context.cancelSelection();
    }
}
