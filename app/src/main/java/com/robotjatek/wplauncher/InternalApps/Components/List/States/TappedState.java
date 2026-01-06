package com.robotjatek.wplauncher.InternalApps.Components.List.States;

import com.robotjatek.wplauncher.AppList.ListItem;
import com.robotjatek.wplauncher.InternalApps.Components.List.ListView;

/**
 * Handles regular tap event then moves to {@link IdleState}
 */
public class TappedState<T> extends BaseState<T> {

    private final float _y;

    public TappedState(ListView<T> context, float y) {
        super(context);
        _y = y;
    }

    @Override
    public void enter() {
        super.enter();
        var tappedItem = getItemAt(_y);
        tappedItem.ifPresent(ListItem::onTap);
        _context.changeState(_context.IDLE_STATE());
    }
}
