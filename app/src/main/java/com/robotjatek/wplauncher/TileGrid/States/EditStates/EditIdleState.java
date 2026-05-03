package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class EditIdleState extends EditBaseState {

    public EditIdleState(EditState context, TileGrid tilegrid) {
        super(context, tilegrid);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        if (_tilegrid.getUnpinButton().isTapped(gesture.getX(), gesture.getY())) {
            _tilegrid.getUnpinButton().onTap();
            return true;
        }

        if (_tilegrid.getResizeButton().isTapped(gesture.getX(), gesture.getY())) {
            _tilegrid.getResizeButton().onTap();
            return true;
        }

        _context.changeState(_tilegrid.IDLE_STATE());
        return true;
    }

//    @Override
//    public void handleTouchStart(float x, float y) {
//        super.handleTouchStart(x, y);
//        _context.changeState(_context.EDIT_READY(x, y));
//    }
//
//    @Override
//    public void handleMove(float x, float y) {
//        super.handleMove(x, y);
//        float deltaX = x - _startX;
//        float deltaY = y - _startY;
//        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//
//        if (distance > MOVEMENT_THRESHOLD) {
//            _context.changeState(_context.EDIT_DRAG(x, y));
//        }
//    }
}
