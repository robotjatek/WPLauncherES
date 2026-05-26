package com.robotjatek.wplauncher.Components.Button.States;

import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;

/**
 * Visual state for button press. Triggers the VISUAL state only, so if a tap was momentary its onTap won't be fired
 */
public class PressedState extends ButtonBaseState {

    private static final float PRESS_DELAY_MS = 100f;
    private static final float MOVE_DELAY_PX = 16f;
    private static final float MOVE_DELAY_PX_SQUARED = MOVE_DELAY_PX * MOVE_DELAY_PX;
    private float _pressDelayRemaining;
    private boolean _pressed; // the visual indication was triggered
    private final float _downX;
    private final float _downY;

    public PressedState(Button context, float downX, float downY) {
        super(context);
        _downX = downX;
        _downY = downY;
    }

    @Override
    public void enter() {
        _pressDelayRemaining = PRESS_DELAY_MS;
        _pressed = false;
    }

    @Override
    public void update(float delta) {
        if (_pressed) {
            return; // do nothing if already shrunk
        }

        _pressDelayRemaining -= delta;
        if (_pressDelayRemaining <= 0) {
            _context.onPress();
            _pressed = true;
        }
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        // abort press on a movement
        var dx = gesture.getX() - _downX;
        var dy = gesture.getY() - _downY;
        if (dx * dx + dy * dy > MOVE_DELAY_PX_SQUARED) {
            abortPress(_pressed);
            _context.changeState(_context.IDLE_STATE());
        }

        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _context.changeState(_context.RELEASE_STATE(_pressed));
        return  true;
    }
}
