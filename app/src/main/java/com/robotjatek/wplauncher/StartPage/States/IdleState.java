package com.robotjatek.wplauncher.StartPage.States;

import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.StartPage.StartScreen;

public class IdleState extends BaseState {

    public IdleState(StartScreen context) {
        super(context);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        _context.getCurrentPage().handleGesture(gesture);
        return true;
    }

    @Override
    public void enter() {
        super.enter();
     //   _context.getCurrentPage().resetState(); TODO: this may have reintroduced the scrolling while swiping bug, keep an eye out
    }
}
