package com.robotjatek.wplauncher.Components.ListView.States.IdleStates;

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

}
