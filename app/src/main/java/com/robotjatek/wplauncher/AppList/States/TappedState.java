package com.robotjatek.wplauncher.AppList.States;

import com.robotjatek.wplauncher.AppList.AppList;
import com.robotjatek.wplauncher.AppList.ListItem;

/**
 * Handles regular tap event then moves to {@link IdleState}
 */
public class TappedState extends BaseState {

    private final float _y;

    public TappedState(AppList context, float y) {
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
