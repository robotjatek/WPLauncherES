package com.robotjatek.wplauncher.Components.ListView.States.IdleStates;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.ListView.States.IdleState;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;

public class IdlePressState<T> extends IdleBaseState<T> {
    private final ListItem<T> _item;
    private final float _downX, _downY;

    public IdlePressState(ListView<T> context, IdleState<T> idle, ListItem<T> item, float downX, float downY) {
        super(context, idle);
        _item = item;
        _downX = downX;
        _downY = downY;
        _item.getTouchHandler().onDown(downX, downY);
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _item.getTouchHandler().onMove(gesture.getX(), gesture.getY());
        if (isMovementExceeded(gesture)) {
            _idle.changeState(_idle.IDLE_READY_STATE());
        }
        return true;
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        if (_context.hasContextMenu()) {
            _item.getTouchHandler().cancel();
            _context.changeState(_context.CONTEXT_MENU_STATE(gesture.getX(), gesture.getY()));
            return true;
        }
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _item.getTouchHandler().onUp();
        _idle.changeState(_idle.IDLE_READY_STATE());
        return true;
    }

    private boolean isMovementExceeded(MoveGesture g) {
        var dx = g.getX() - _downX;
        var dy = g.getY() - _downY;
        return (dx * dx + dy * dy) > TouchHandler.MOVE_THRESHOLD_SQUARED;
    }
}
