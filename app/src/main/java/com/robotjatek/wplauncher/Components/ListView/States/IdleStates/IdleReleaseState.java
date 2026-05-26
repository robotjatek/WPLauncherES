package com.robotjatek.wplauncher.Components.ListView.States.IdleStates;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.ListView.States.IdleState;

public class IdleReleaseState<T> extends IdleBaseState<T> {

    private final ListItem<T> _item;
    private final boolean _pressAlreadyVisible;
    private float _remainingMs;
    private boolean _finished = false;

    public IdleReleaseState(ListView<T> list, IdleState<T> idle, ListItem<T> item, boolean pressAlreadyVisible) {
        super(list, idle);
        _item = item;
        _pressAlreadyVisible = pressAlreadyVisible;
    }

    @Override
    public void enter() {
        if (!_pressAlreadyVisible) {
            _item.onPress();
        }
        _remainingMs = _pressAlreadyVisible ? 0f : 90f;
        _finished = false;
    }

    @Override
    public void exit() {
        if (_finished) {
            abortPress(_item, true);
        }
    }

    @Override
    public void update(float delta) {
        _remainingMs -= delta;
        if (_remainingMs <= 0f) {
            _item.onRelease(true);
            _finished = true;
            _idle.changeState(_idle.IDLE_READY_STATE());
        }
    }
}
