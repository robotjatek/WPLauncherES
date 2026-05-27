package com.robotjatek.wplauncher.Components;

public class TouchHandler {
    private enum State {
        IDLE,
        WAIT_FOR_PRESS,
        PRESS_ACTIVE,
        WAIT_FOR_ACTION
    }

    private static final float PRESS_DELAY = 100f;
    private static final float MIN_PRESS_TIME = 50f;
    private static final float ACTION_DELAY = 50f;
    public static final float MOVE_THRESHOLD_SQUARED = 16f * 16f;

    private final ITouchable _target;
    private State _state = State.IDLE;
    private float _timer = 0;
    private boolean _fingerDown = false;
    private float _downX, _downY;

    public TouchHandler(ITouchable target) {
        _target = target;
    }

    public void onDown(float x, float y) {
        _downX = x;
        _downY = y;
        _fingerDown = true;
        _state = State.WAIT_FOR_PRESS;
        _timer = PRESS_DELAY;
    }

    public void onUp() {
        _fingerDown = false;
        if (_state == State.WAIT_FOR_PRESS) {
            _timer = 0;
        }
    }

    public void onMove(float x, float y) {
        if (_state == State.WAIT_FOR_PRESS || _state == State.PRESS_ACTIVE) {
            float dx = x - _downX;
            float dy = y - _downY;
            if (dx * dx + dy * dy > MOVE_THRESHOLD_SQUARED) {
                cancel();
            }
        }
    }

    public void update(float delta) {
        if (_state == State.IDLE) return;

        if (_timer > 0) {
            _timer -= delta;
        }

        switch (_state) {
            case WAIT_FOR_PRESS:
                if (_timer <= 0) {
                    _target.onPress();
                    _state = State.PRESS_ACTIVE;
                    _timer = MIN_PRESS_TIME;
                }
                break;

            case PRESS_ACTIVE:
                if (_timer <= 0 && !_fingerDown) {
                    _target.onRelease();
                    _state = State.WAIT_FOR_ACTION;
                    _timer = ACTION_DELAY;
                }
                break;

            case WAIT_FOR_ACTION:
                if (_timer <= 0) {
                    _target.onAction();
                    _state = State.IDLE;
                }
                break;
        }
    }

    public void cancel() {
        if (_state == State.PRESS_ACTIVE) {
            _target.onRelease();
        }
        _state = State.IDLE;
        _fingerDown = false;
        _timer = 0;
    }
}