package com.robotjatek.wplauncher.TileGrid.States;

import android.util.Log;

import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

import java.util.Optional;

public class BaseState implements IState {

    protected TileGrid _context;

    public BaseState(TileGrid context) {
        _context = context;
    }

    @Override
    public void enter() {
        Log.d(IState.class.toString(), this.getClass().toString());
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

    public Optional<Tile> getTileAt(float x, float y) {
        return _context.getTiles().stream().filter(t -> {
            var scrollPosition = _context.getScroll().getScrollOffset();
            var left = _context.getDrawContext().xOf(t);
            var top = _context.getDrawContext().yOf(t) + scrollPosition + TileGrid.TOP_MARGIN_PX;
            var right = left + _context.getDrawContext().widthOf(t);
            var bottom = top + _context.getDrawContext().heightOf(t);

            return x >= left && x <= right && y >= top && y <= bottom;
        }).findFirst();
    }
}
