package com.robotjatek.wplauncher.TileGrid.States.IdleStates;

import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.TileGrid.States.IdleState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class IdlePressState extends IdleBaseState {
    private static final float MOVE_DELAY_PX = 16f;
    private static final float MOVE_DELAY_SQUARED = MOVE_DELAY_PX * MOVE_DELAY_PX;
    private final Tile _tile;
    private final float _downX;
    private final float _downY;

    public IdlePressState(IdleState idle, TileGrid tileGrid, Tile tile, float downX, float downY) {
        super(idle, tileGrid);
        _tile = tile;
        _downX = downX;
        _downY = downY;
        tile.getTouchHandler().onDown(downX, downY);
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _tile.getTouchHandler().onMove(gesture.getX(), gesture.getY());
        var dx = gesture.getX() - _downX;
        var dy = gesture.getY() - _downY;
        if (dx * dx + dy * dy > MOVE_DELAY_SQUARED) {
            _idle.changeState(_idle.READY());
        }
        return true;
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        _tile.getTouchHandler().cancel();
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture);
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        _tile.getTouchHandler().cancel();
        _context.changeState(_context.EDIT_STATE(gesture.getX(), gesture.getY()));
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _tile.getTouchHandler().onUp();
        _idle.changeState(_idle.READY());
        return true;
    }
}
