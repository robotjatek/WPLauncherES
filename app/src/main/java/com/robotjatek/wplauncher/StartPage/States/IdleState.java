package com.robotjatek.wplauncher.StartPage.States;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.StartPage.StartScreen;

public class IdleState extends BaseState {

    public IdleState(StartScreen context) {
        super(context);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        if (_context.isChildrenCatchingGestures()) {
            _context.changeState(_context.CHILD_CONTROL_STATE());
            return _context.handleGesture(gesture);
        }
        return super.handleGesture(gesture);
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        _context.getCurrentPage().handleGesture(gesture);
        return true;
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        _context.getCurrentPage().handleGesture(gesture);
        return true;
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        if (Math.abs(gesture.getDx()) > Math.abs(gesture.getDy())) {
            _context.changeState(_context.SWIPE_STATE(gesture.getX()));
        } else {
            _context.changeState(_context.SCROLL_STATE());
        }
        return _context.handleGesture(gesture);
    }

    @Override
    public void enter() {
        super.enter();
     //   _context.getCurrentPage().resetState(); TODO: this may have reintroduced the scrolling while swiping bug, keep an eye out
    }
}
