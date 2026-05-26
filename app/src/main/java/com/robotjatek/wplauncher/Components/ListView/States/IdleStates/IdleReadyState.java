package com.robotjatek.wplauncher.Components.ListView.States.IdleStates;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.ListView.States.IdleState;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;

public class IdleReadyState<T> extends IdleBaseState<T> {

    public IdleReadyState(ListView<T> context, IdleState<T> idle) {
        super(context, idle);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        var item = _idle.getItemAt(gesture.getX(), gesture.getY());
        item.ifPresent(ListItem::onTap);
        return true;
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture); // delegate the gesture to scroll state
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        if (_context.hasContextMenu()) {
            _context.changeState(_context.CONTEXT_MENU_STATE(gesture.getX(), gesture.getY()));
            return true; // menu opened, we consumed the interaction
        }
        return false; // no menu to open, tell the parent we didn't do anything with that gesture
    }

}
