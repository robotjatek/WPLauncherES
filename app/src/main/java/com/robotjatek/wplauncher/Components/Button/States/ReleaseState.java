package com.robotjatek.wplauncher.Components.Button.States;

import com.robotjatek.wplauncher.Components.Button.Button;

/**
 * Shows the released visible state for a given time, then schedules the onTap event
 */
public class ReleaseState extends ButtonBaseState {

    private final boolean _pressAlreadyVisible;
    private float _remainingMs;
    private boolean _finished = false;

    public ReleaseState(Button context, boolean pressAlreadyVisible) {
        super(context);
        _pressAlreadyVisible = false;
    }

    @Override
    public void enter() {
        if (!_pressAlreadyVisible) {
            _context.onPress();
        }
        _remainingMs = _pressAlreadyVisible ? 0f : 90f;
        _finished = false;
    }

    @Override
    public void update(float delta) {
        _remainingMs -= delta;
        if (_remainingMs <= 0) {
            _context.onRelease(true);
            _finished = true;
            _context.changeState(_context.IDLE_STATE());
        }
    }

    @Override
    public void exit() {
        if (_finished) {
            abortPress(true);
        }
    }
}
