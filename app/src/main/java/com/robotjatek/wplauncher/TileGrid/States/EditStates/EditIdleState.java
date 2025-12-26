package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class EditIdleState extends EditBaseState {
    private static final float MOVEMENT_THRESHOLD = 15;
    private final float _startX;
    private final float _startY;

    public EditIdleState(EditState context, TileGrid tilegrid, float x, float y) {
        super(context, tilegrid);
        _startX = x;
        _startY = y;
    }

    @Override
    public void handleTouchStart(float x, float y) {
        super.handleTouchStart(x, y);
        _context.changeState(_context.EDIT_READY(x, y));
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
