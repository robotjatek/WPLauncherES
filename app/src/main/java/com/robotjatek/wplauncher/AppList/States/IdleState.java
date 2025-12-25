package com.robotjatek.wplauncher.AppList.States;

import com.robotjatek.wplauncher.AppList.AppList;


public class IdleState extends BaseState {

    public IdleState(AppList context) {
        super(context);
    }

    @Override
    public void handleTouchStart(float x, float y) {
        _context.changeState(_context.TOUCHING_STATE(x, y));
    }
}
