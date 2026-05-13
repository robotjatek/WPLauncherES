package com.robotjatek.wplauncher.Gestures;

import com.robotjatek.wplauncher.IUIContext;

public class ScrollGesture extends Gesture {

    private final float _dx, _dy;

    public ScrollGesture(float x, float y, float dx, float dy, IUIContext uiContext) {
        super(x, y, uiContext);
        _dx = dx;
        _dy = dy;
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleScroll(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new ScrollGesture(_x + dx, _y + dy, _dx, _dy, _uiContext);
    }

    public float getDx() {
        return _dx;
    }

    public float getDy() {
        return _dy;
    }
}
