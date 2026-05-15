package com.robotjatek.wplauncher.Gestures;

import com.robotjatek.wplauncher.IUIContext;

public abstract class Gesture {
    protected float _x, _y;
    protected IUIContext _uiContext;

    protected Gesture(float x, float y, IUIContext uiContext) {
        _x = x;
        _y = y;
        _uiContext = uiContext;
    }

    public abstract boolean dispatch(IGestureHandler handler);
    public abstract Gesture copyWithOffset(float dx, float dy);

    public float getX() {
        return _x;
    }

    public float getY() {
        return _y;
    }

    public IUIContext getUIContext() {
        return _uiContext;
    }
}
