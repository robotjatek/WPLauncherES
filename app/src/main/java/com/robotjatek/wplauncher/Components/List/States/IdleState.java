package com.robotjatek.wplauncher.Components.List.States;

import com.robotjatek.wplauncher.Components.List.ListView;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
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

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture);
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        _context.changeState(_context.CONTEXT_MENU_STATE(gesture.getX(), gesture.getY()));
        return _context.handleLongPress(gesture);
    }
}
