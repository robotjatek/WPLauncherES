package com.robotjatek.wplauncher.Components.ListView.States.IdleStates;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.ListView.States.IdleState;
import com.robotjatek.wplauncher.IState;

public abstract class IdleBaseState<T> implements IState {

    protected final ListView<T> _context;
    protected final IdleState<T> _idle;

    protected IdleBaseState(ListView<T> list, IdleState<T> idle) {
        _context = list;
        _idle = idle;
    }

    @Override
    public void enter(){}

    @Override
    public void exit(){}

    @Override
    public void update(float delta) {}

    protected void abortPress(ListItem<T> item, boolean shrunk) {
        if (shrunk) {
            // if the item was shrunk, unshrunk it, and DO NOT fire the tap event
            // this is called when the item was only tapped for a brief period. E.g. right before scrolling
            item.onRelease(false);
        } else {
            // if a fire event was scheduled, cancel it
            item.cancelPendingTap();
        }
    }

}
