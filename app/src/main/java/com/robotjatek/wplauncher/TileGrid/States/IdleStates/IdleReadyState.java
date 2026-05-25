package com.robotjatek.wplauncher.TileGrid.States.IdleStates;

import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.States.IdleState;
import com.robotjatek.wplauncher.TileGrid.States.ScrollState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

/**
 * Default idle state.
 * In this state the tile grid is waiting for user interaction.
 * Transitions:
 * - {@link IdlePressState} when the user presses a tile
 * - {@link ScrollState} when a scroll gesture begins
 * - {@link EditState} on long press
 * Tap gestures are consumed but otherwise ignored, as tile activation is
 * handled through the press/release state sequence.
 */
public class IdleReadyState extends IdleBaseState {

    public IdleReadyState(IdleState idle, TileGrid tileGrid) {
        super(idle, tileGrid);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
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
    public boolean handleDown(DownGesture gesture) {
        getTileAt(gesture.getX(), gesture.getY())
                .ifPresent(t -> _idle.changeState(_idle.PRESS(t, gesture.getX(), gesture.getY())));
        return true;
    }
}
