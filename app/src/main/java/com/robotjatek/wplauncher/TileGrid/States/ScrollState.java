package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class ScrollState extends BaseState {
    private final float _startY;
    private boolean _touching = true;

    public ScrollState(TileGrid context, float y) {
        super(context);
        _startY = y;
    }

    @Override
    public void enter() {
        super.enter();
        _context.getScroll().onTouchStart(_startY);
        _touching = true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        if (_touching) {
            _context.getScroll().onTouchMove(gesture.getY());
            return true;
        }

        return false;
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        _context.getScroll().onTouchMove(gesture.getY());
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _context.getScroll().onTouchEnd();
        _touching = false;
        return true;
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        _context.getScroll().onTouchStart(gesture.getY());
        _touching = true;
        return true;
    }

    @Override
    public boolean isCatchingGestures() {
        return _touching || _context.getScroll().isFlinging();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!_touching && !_context.getScroll().isFlinging()) {
            _context.changeState(_context.IDLE_STATE());
        }
    }
}
