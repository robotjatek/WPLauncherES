package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.IGestureState;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public abstract class EditBaseState implements IGestureState {

    protected EditState _context;
    protected final TileGrid _tilegrid;

    protected EditBaseState(EditState context, TileGrid tileGrid) {
        _context = context;
        _tilegrid = tileGrid;
    }

    @Override
    public void enter() {
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleTouchStart(float x, float y) {
    }

    @Override
    public void handleTouchEnd(float x, float y) {
    }

    @Override
    public void handleMove(float x, float y) {
    }

    @Override
    public void update(float delta) {

    }
}
