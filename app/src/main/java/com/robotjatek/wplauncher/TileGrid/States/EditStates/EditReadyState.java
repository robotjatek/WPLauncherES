package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class EditReadyState extends EditBaseState {

    private static final float MOVEMENT_THRESHOLD = 15;
    private final float _startX;
    private final float _startY;

    public EditReadyState(EditState context, TileGrid tileGrid, float x, float y) {
        super(context, tileGrid);
        _startX = x;
        _startY = y;
    }

    @Override
    public void handleTouchStart(float x, float y) {
        super.handleTouchStart(x, y);
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        super.handleTouchEnd(x, y);
        // tap adorner
        if (_tilegrid.getUnpinButton()
                .isTapped(x, y - _tilegrid.getScroll().getScrollOffset() - TileGrid.TOP_MARGIN_PX)) {
            _tilegrid.getUnpinButton().onTap();
            _tilegrid.changeState(_tilegrid.IDLE_STATE());
            return;
        }

        // Check if we tapped the same tile again or an empty space
        // (If we reached that point, there is already a selected tile)
        var tappedTile = _context.getTileAt(x, y);
        if (tappedTile.isEmpty() || tappedTile.get() == _tilegrid.getSelectedTile()) {
            // empty space or same tile was clicked: unselect and go back to idle
            _tilegrid.cancelSelection();
            _tilegrid.changeState(_tilegrid.IDLE_STATE());
        } else {
            _tilegrid.selectTile(tappedTile.get());
        }
    }

    @Override
    public void handleMove(float x, float y) {
        super.handleMove(x, y);
        float deltaX = x - _startX;
        float deltaY = y - _startY;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance > MOVEMENT_THRESHOLD) {
            _context.changeState(_context.EDIT_DRAG(x, y));
        }
    }
}
