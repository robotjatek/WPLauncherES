package com.robotjatek.wplauncher.Gestures;

public class TapGesture extends Gesture {
    public TapGesture(float x, float y) {
        super(x, y);
    }

    @Override
    public boolean dispatch(IGestureHandler element) {
        return element.handleTap(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new TapGesture(_x + dx, _y + dy);
    }
}
