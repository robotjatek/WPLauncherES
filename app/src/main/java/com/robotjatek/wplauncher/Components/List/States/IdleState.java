package com.robotjatek.wplauncher.Components.List.States;

import com.robotjatek.wplauncher.Components.List.ListView;

public class IdleState<T> extends BaseState<T> {

    public IdleState(ListView<T> context) {
        super(context);
    }

    @Override
    public void handleTouchStart(float x, float y) {
        _context.changeState(_context.TOUCHING_STATE(x, y));
    }
}
