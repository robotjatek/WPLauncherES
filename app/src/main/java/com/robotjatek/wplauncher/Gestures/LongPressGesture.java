package com.robotjatek.wplauncher.Gestures;

public class LongPressGesture extends Gesture {

    public LongPressGesture(float x, float y) {
        super(x, y);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleLongPress(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new LongPressGesture(_x + dx, _y + dy);
    }
}
