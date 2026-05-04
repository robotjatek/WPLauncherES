package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class EditIdleState extends EditBaseState {
    private float _startX;
    private float _startY;

    public EditIdleState(EditState context, TileGrid tilegrid, float x, float y) {
        super(context, tilegrid);
        _startX = x;
        _startY = y;
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        // Update the start coordinates on retouch, so we don't accidentally move to EditDragState
        _startX = gesture.getX();
        _startY = gesture.getY();
        return true;
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        // tap adorner
        if (_tilegrid.getUnpinButton().isTapped(
                gesture.getX(),
                gesture.getY() - _tilegrid.getScroll().getScrollOffset() - TileGrid.TOP_MARGIN_PX)) {
            _tilegrid.getUnpinButton().onTap();
            _tilegrid.changeState(_tilegrid.IDLE_STATE());
            return true;
        }

        if (_tilegrid.getResizeButton().isTapped(
                gesture.getX(),
                gesture.getY() - _tilegrid.getScroll().getScrollOffset() - TileGrid.TOP_MARGIN_PX)) {
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

        // Check if we tapped the same tile again or an empty space
        // (If we reached that point, there is already a selected tile)
        var tappedTile = _context.getTileAt(gesture.getX(), gesture.getY());
        if (tappedTile.isEmpty() || tappedTile.get() == _tilegrid.getSelectedTile()) {
            // empty space or same tile was clicked: unselect and go back to idle
            _tilegrid.cancelSelection();
            _tilegrid.changeState(_tilegrid.IDLE_STATE());
        } else {
            _tilegrid.selectTile(tappedTile.get());
        }

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
