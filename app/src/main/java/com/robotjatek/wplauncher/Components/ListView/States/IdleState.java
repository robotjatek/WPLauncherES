package com.robotjatek.wplauncher.Components.ListView.States;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.ListView.States.IdleStates.IdlePressState;
import com.robotjatek.wplauncher.Components.ListView.States.IdleStates.IdleReadyState;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IState;

/**
 * Hierarchical state machine to represent idle state.
 * Its purpose is to delegate gestures and calls to its substates
 * @param <T> The type of the payload of the list
 */
public class IdleState<T> extends BaseState<T> {

    public IState IDLE_READY_STATE() {
        return new IdleReadyState<>(_context, this);
    }

    public IState PRESS_STATE(ListItem<T> item, float downX, float downY) {
        return new IdlePressState<>(_context, this, item, downX, downY);
    }

    private IState _state = IDLE_READY_STATE();

    public IdleState(ListView<T> context) {
        super(context);
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    @Override
    public void enter() {
        super.enter();
        _state.enter();
    }

    @Override
    public void exit() {
        super.exit();
        _state.exit();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        _state.update(delta);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
    }
}
