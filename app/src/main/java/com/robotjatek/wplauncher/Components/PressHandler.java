package com.robotjatek.wplauncher.Components;

public class PressHandler {
    private static final float PRESS_VISUAL_DELAY_MS = 100f;
    private static final float ACTION_DELAY_MS = 50f;
    private static final float MOVE_THRESHOLD_PX = 16f;
    private static final float MOVE_THRESHOLD_SQUARED = MOVE_THRESHOLD_PX * MOVE_THRESHOLD_PX;

    private final ITouchable _target;
    private float _timer = 0;
    private boolean _isPressed = false;
    private boolean _isWaitingForAction = false;
    private float _downX;
    private float _downY;

    public PressHandler(ITouchable target) {
        _target = target;
    }

    public void onDown(float x, float y) {
        reset();
        _downX = x;
        _downY = y;
        _timer = PRESS_VISUAL_DELAY_MS;
    }

    public void onUp() {
        if (_isPressed) {
            _target.onRelease(false);
            _isPressed = false;
            _isWaitingForAction = true;
            _timer = ACTION_DELAY_MS;
        } else if (_timer > 0) {
            _target.onPress();
            _isWaitingForAction = true;
            _timer = ACTION_DELAY_MS;
        } else {
            cancel();
        }
    }

    public void onMove(float x, float y) {
        if (_isWaitingForAction) {
            return;
        }

        var dx = x - _downX;
        var dy = y - _downY;
        if (dx * dx + dy * dy > MOVE_THRESHOLD_SQUARED) {
            cancel();
        }
    }

    public void update(float delta) {
        if (_timer <= 0) {
            return;
        }

        _timer -= delta;
        if (_timer <= 0) {
            if (!_isPressed && !_isWaitingForAction) {
                _isPressed = true;
                _target.onPress();
            } else if (_isWaitingForAction) {
                _target.onRelease(true);
                reset();
            }
        }
    }

    public void cancel() {
        if (_isPressed) {
            _target.onRelease(false);
        }
        reset();
    }

    private void reset() {
        _timer = 0;
        _isPressed = false;
        _isWaitingForAction = false;
    }

}
