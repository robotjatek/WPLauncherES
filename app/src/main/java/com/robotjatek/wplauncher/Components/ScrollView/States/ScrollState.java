package com.robotjatek.wplauncher.Components.ScrollView.States;

import com.robotjatek.wplauncher.Components.ScrollView.ScrollView;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;

public class ScrollState extends BaseState {

    private final float _startY;
    private boolean _touching = true;

    public ScrollState(ScrollView context, float startY) {
        super(context);
        _startY = startY;
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
    public boolean handleDown(DownGesture gesture) {
        _context.getScroll().onTouchStart(gesture.getY());
        _touching = true;
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (!_touching && !_context.getScroll().isFlinging()) {
            _context.changeState(_context.IDLE_STATE());
        }
    }

    @Override
    public boolean isCatchingGestures() {
        return true;
    }

}
