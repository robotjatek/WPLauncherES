package com.robotjatek.wplauncher.StartPage.States;

import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.StartPage.StartScreen;

/**
 * {@link ScrollState} Relays move intent to the children and disregards any other input
 * Transitions to {@link IdleState} when the move ends
 */
public class ScrollState extends BaseState {

    public ScrollState(StartScreen context) {
        super(context);
    }

    // TODO: ez itt a fel-le scroll, még tesztelni kell
    // TODO: handle scroll lesz ez szerintem
    @Override
    public boolean handleMove(MoveGesture gesture) {
        return _context.getCurrentPage().handleGesture(gesture);
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _context.changeState(_context.IDLE_STATE());
        return true;
    }
}
