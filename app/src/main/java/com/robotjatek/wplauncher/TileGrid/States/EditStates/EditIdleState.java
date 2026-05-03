package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.Gestures.MoveGesture;
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

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _context.changeState(_context.EDIT_DRAG(gesture.getX(), gesture.getY()));
        return true;
        //return _context.handleGesture(gesture); // TODO: ?
    }
}
