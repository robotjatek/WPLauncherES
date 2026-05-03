package com.robotjatek.wplauncher.Gestures;

public abstract class Gesture {
    protected float _x, _y;

    protected Gesture(float x, float y) {
        _x = x;
        _y = y;
    }

    public abstract boolean dispatch(IGestureHandler handler);
    public abstract Gesture copyWithOffset(float dx, float dy);

    public float getX() {
        return _x;
    }

    public float getY() {
        return _y;
    }
}
