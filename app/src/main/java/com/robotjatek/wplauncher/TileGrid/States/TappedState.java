package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class TappedState extends BaseState {
    private final float _x;
    private final float _y;

    public TappedState(TileGrid context, float x, float y) {
        super(context);
        _x = x;
        _y = y;
    }

    @Override
    public void enter() {
        super.enter();
        var tappedTile = getTileAt(_x, _y);
        tappedTile.ifPresent(Tile::onTap);
        _context.changeState(_context.IDLE_STATE());
    }
}
