package com.robotjatek.wplauncher.AppList.States;

import com.robotjatek.wplauncher.AppList.AppList;
import com.robotjatek.wplauncher.ScrollController;

public class ScrollState extends BaseState {
    private final float _startY;

    public ScrollState(AppList context, float y) {
        super(context);
        _startY = y;
    }

    @Override
    public void enter() {
        super.enter();
        _context.getScroll().onTouchStart(_startY);
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        _context.getScroll().onTouchEnd();
        _context.changeState(_context.IDLE_STATE());
    }

    @Override
    public void handleMove(float x, float y) {
        _context.getScroll().onTouchMove(y);
    }
}
