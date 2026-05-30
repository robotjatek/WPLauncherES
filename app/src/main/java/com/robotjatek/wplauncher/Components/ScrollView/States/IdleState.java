package com.robotjatek.wplauncher.Components.ScrollView.States;

import com.robotjatek.wplauncher.Components.ScrollView.ScrollView;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;

public class IdleState extends BaseState {

    public IdleState(ScrollView context) {
        super(context);
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture);
    }
}
