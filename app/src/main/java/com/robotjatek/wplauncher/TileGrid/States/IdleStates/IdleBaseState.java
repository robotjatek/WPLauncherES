package com.robotjatek.wplauncher.TileGrid.States.IdleStates;

import com.robotjatek.wplauncher.TileGrid.States.BaseState;
import com.robotjatek.wplauncher.TileGrid.States.IdleState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public abstract class IdleBaseState extends BaseState {
    protected final IdleState _idle;

    protected IdleBaseState(IdleState idle, TileGrid tileGrid) {
        super(tileGrid);
        _idle = idle;
    }

    protected static void abortPress(Tile tile, boolean shrunk) {
        if (shrunk) {
            tile.onRelease(false);
        } else {
            tile.cancelPendingTap();
        }
    }
}
