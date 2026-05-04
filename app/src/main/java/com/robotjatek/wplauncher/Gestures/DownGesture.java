package com.robotjatek.wplauncher.Gestures;

public class DownGesture extends Gesture {

    public DownGesture(float x, float y) {
        super(x, y);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleDown(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new DownGesture(_x + dx, _y + dy);
    }
}
