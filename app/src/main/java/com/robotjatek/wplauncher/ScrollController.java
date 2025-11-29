package com.robotjatek.wplauncher;

public class ScrollController {

    private static final float FRICTION = 0.0025f; // Deceleration in pixels/ms2
    private static final float FLING_THRESHOLD = 1.5f;
    private static final float MIN_VELOCITY = 0.01f;
    private static final float MAX_VELOCITY = 10f;

    private boolean _isFlinging = false;
    private float _lastY = 0;
    private float _velocity = 0; // pixels per millisecond
    private long _lastTouchTime = 0;
    private float _minOffset = 0;
    private float _maxOffset = 0;
    private float _scrollOffset = 0;
    private float _smoothDelta = 16.67f;
    private float[] _recentVelocities = new float[3];
    private int _velocityIndex = 0;

    public void update(float delta) {
        if (!_isFlinging) {
            return;
        }

        _smoothDelta = _smoothDelta * 0.8f + delta * 0.2f;
        var useDelta = _smoothDelta;

        // Apply deceleration (friction acts like negative acceleration)
        var deceleration = FRICTION * useDelta;
        if (_velocity > 0) {
            _velocity -= deceleration;
            if (_velocity < 0) {
                _velocity = 0;
            }
        } else if (_velocity < 0) {
            _velocity += deceleration;
            if (_velocity > 0) {
                _velocity = 0;
            }
        }

        // Stop if velocity is below threshold
        if (Math.abs(_velocity) < MIN_VELOCITY) {
            _velocity = 0;
            _isFlinging = false;
            return;
        }

        // Update scroll position
        _scrollOffset += _velocity * useDelta;
        _scrollOffset = clampOffset(_scrollOffset);
    }

    public void onTouchStart(float y) {
        _isFlinging = false;
        _velocity = 0;
        _lastY = y;
        _lastTouchTime = System.currentTimeMillis();
    }

    // Updates the scroll position while the finger is on the screen and maintains a gradually decreasing velocity for "fling" gestures
    public void onTouchMove(float y) {
        var now = System.currentTimeMillis();
        var dy = y - _lastY;

        // Update scroll position immediately
        _scrollOffset += dy;
        _scrollOffset = clampOffset(_scrollOffset);

        // Calculate velocity in pixels per millisecond
        var dt = now - _lastTouchTime;
        if (dt > 0) {
            _recentVelocities[_velocityIndex] = dy / dt;
            _velocityIndex = (_velocityIndex + 1) % 3;
            _velocity = (_recentVelocities[0] + _recentVelocities[1] + _recentVelocities[2]) / 3f;
            _velocity = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, _velocity));
            // Clamp velocity to prevent extreme values
            _velocity = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, _velocity));
        }

        _lastY = y;
        _lastTouchTime = now;
    }

    public void onTouchEnd() {
        // Start fling if velocity exceeds threshold
        if (Math.abs(_velocity) >= FLING_THRESHOLD) {
            _isFlinging = true;
        } else {
            _velocity = 0;
            _isFlinging = false;
        }
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