package com.robotjatek.wplauncher.Components.ListView.States.IdleStates;

import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.ListView.States.IdleState;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;

public class IdleReadyState<T> extends IdleBaseState<T> {

    public IdleReadyState(ListView<T> context, IdleState<T> idle) {
        super(context, idle);
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture); // delegate the gesture to scroll state
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        _idle.getItemAt(gesture.getX(), gesture.getY())
                .ifPresent(i -> _idle.changeState(_idle.PRESS_STATE(i, gesture.getX(), gesture.getY())));
        return true;
    }

}
