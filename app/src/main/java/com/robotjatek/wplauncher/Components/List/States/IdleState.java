package com.robotjatek.wplauncher.Components.List.States;

import com.robotjatek.wplauncher.Components.List.ListView;
import com.robotjatek.wplauncher.Gestures.TapGesture;

public class IdleState<T> extends BaseState<T> {

    public IdleState(ListView<T> context) {
        super(context);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        _context.changeState(_context.TAPPED_STATE(gesture.getY()));
        return true;
    }
}
