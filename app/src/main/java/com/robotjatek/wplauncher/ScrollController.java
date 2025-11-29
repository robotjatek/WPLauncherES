package com.robotjatek.wplauncher;

public class ScrollController {

    private static final float FRICTION = 0.01f;
    private static final float FLING_START_VELOCITY = 1f;
    private boolean _isFlinging = false;
    private float _lastY= 0;
    private float _velocity = 0;
    private long _startTime = 0;
    private float _minOffset = 0;
    private float _maxOffset = 0;

    private float _scrollOffset = 0;

    public void update(float delta) {
        // Return immediately no animation and no deceleration is needed.
        if (!_isFlinging) {
            return;
        }

        // Gradually decelerate, based on the elapsed time
        if (_velocity > 0) {
            _velocity -= FRICTION * delta;
            if (_velocity < 0) {
                _velocity = 0;
            }
        } else if (_velocity < 0) {
            _velocity += FRICTION * delta;
            if (_velocity > 0) {
                _velocity = 0;
            }
        }

        // set the new scroll position considering the delta time
        _scrollOffset += _velocity * delta;
        //confine into viewport
        _scrollOffset = clampOffset(_scrollOffset);
    }

    public void onTouchStart(float y) {
        // Stop flinging on a new touch
        _isFlinging = false;
        _velocity = 0;
        _lastY = y;
        _startTime = System.currentTimeMillis();
    }

    public void onTouchMove(float y) {
        // calculate movement vector, follow finger while touching
        // velocity is the distance change over the elapsed time
        // velocity is gradually decreased in the update method
        var now = System.currentTimeMillis();
        var dy = y - _lastY;
        _scrollOffset += dy;
        var dt = now - _startTime;
        _velocity = dy / dt;

        _scrollOffset = clampOffset(_scrollOffset);

        _lastY = y;
        _startTime = now;
    }

    public void onTouchEnd() {
        if (Math.abs(_velocity) < FLING_START_VELOCITY) {
            _velocity = 0f;
            _isFlinging = false;
            return;
        }
        _isFlinging = true;
    }


    public float getScrollOffset() {
        return _scrollOffset;
    }

    public void setBounds(float min, float max) {
        _minOffset = min;
        _maxOffset = max;
        _scrollOffset = clampOffset(_scrollOffset);
    }

    private float clampOffset(float value) {
        return Math.max(_minOffset, Math.min(_maxOffset, value));
    }
}
