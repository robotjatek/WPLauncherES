package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class EditIdleState extends EditBaseState {
    private final float _startX;
    private final float _startY;

    public EditIdleState(EditState context, TileGrid tilegrid, float x, float y) {
        super(context, tilegrid);
        _startX = x;
        _startY = y;
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        if (_tilegrid.getUnpinButton().isTapped(gesture.getX(), gesture.getY())) {
            _tilegrid.getUnpinButton().onTap();
            return true;
        }

        var scrollOffset = _tilegrid.getScroll().getScrollOffset();
        if (_tilegrid.getResizeButton().isTapped(gesture.getX(), gesture.getY() - scrollOffset - TileGrid.TOP_MARGIN_PX)) {
            _tilegrid.getResizeButton().onTap();
            var selectedTile = _tilegrid.getSelectedTile();
            var position = new Position<>(selectedTile.getPosition().x(), selectedTile.getPosition().y());
            if (!isInbounds(position)) {
                // remove everything from the column => move to the first column
                var correctedPosition = new Position<>(0, selectedTile.getPosition().y());
                reflowTiles(correctedPosition);
            } else {
                reflowTiles(position);
            }
            return true;
        }

        _context.changeState(_tilegrid.IDLE_STATE());
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        var deltaX = gesture.getX() - _startX;
        var deltaY = gesture.getY() - _startY;
        var distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance > 40) {
            _context.changeState(_context.EDIT_DRAG(gesture.getX(), gesture.getY()));
        }

        return true;
    }
}
