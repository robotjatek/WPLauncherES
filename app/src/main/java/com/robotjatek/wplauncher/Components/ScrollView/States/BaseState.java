package com.robotjatek.wplauncher.Components.ScrollView.States;

import com.robotjatek.wplauncher.Components.ScrollView.ScrollView;
import com.robotjatek.wplauncher.IState;

public abstract class BaseState implements IState {

    protected final ScrollView _context;

    protected BaseState(ScrollView context) {
        _context = context;
    }

    @Override
    public void enter() {
    }

    @Override
    public void exit() {
    }

    @Override
    public void update(float delta) {
        _context.getScroll().update(delta);
    }
}
