package com.robotjatek.wplauncher.Components.List.States;

import com.robotjatek.wplauncher.Components.List.ListView;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;

public class ScrollState<T> extends BaseState<T> {
    private final float _startY;
    private boolean _touching = true;

    public ScrollState(ListView<T> context, float y) {
        super(context);
        _startY = y;
    }

    @Override
    public void enter() {
        super.enter();
        _context.getScroll().onTouchStart(_startY);
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
    public void update(float delta) {
        super.update(delta);
        if (!_touching && !_context.getScroll().isFlinging()) {
            _context.changeState(_context.IDLE_STATE());
        }
    }
}
