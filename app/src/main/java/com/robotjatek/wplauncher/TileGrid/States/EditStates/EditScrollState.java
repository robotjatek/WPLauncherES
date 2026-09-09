package com.robotjatek.wplauncher.TileGrid.States.EditStates;

import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class EditScrollState extends EditBaseState {

    private final float _startY;
    private float _currentX;
    private float _currentY;
    private boolean _touching = true;

    public EditScrollState(EditState context, TileGrid tileGrid, float startX, float startY) {
        super(context, tileGrid);
        _startY = startY;
        _currentX = startX;
        _currentY = startY;
    }

    @Override
    public void enter() {
        super.enter();
        _tilegrid.getScroll().onTouchStart(_startY);
        _touching = true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _currentX = gesture.getX();
        _currentY = gesture.getY();

        if (_touching) {
            _tilegrid.getScroll().onTouchMove(gesture.getY());
            return true;
        }

        return false;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _tilegrid.getScroll().onTouchEnd();
        _touching = false;
        return true;
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        _tilegrid.getScroll().onTouchStart(gesture.getY());
        _touching = true;
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!_touching && !_tilegrid.getScroll().isFlinging()) {
            _context.changeState(_context.EDIT_IDLE(_currentX, _currentY));
        }
    }
}
