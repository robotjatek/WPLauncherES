package com.robotjatek.wplauncher.Components.ListView.States;

import com.robotjatek.wplauncher.Components.ListPage.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;

public class IdleState<T> extends BaseState<T> {
    public IdleState(ListView<T> context) {
        super(context);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        var item = getItemAt(gesture.getX(),gesture.getY());
        item.ifPresent(ListItem::onTap);
        return true;
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture);
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
//        if (_context.hasContextMenu()) {
//
//        }
//        return _context.handleLongPress(gesture); // TODO: return true?
        return true;
    }
}
