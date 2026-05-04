package com.robotjatek.wplauncher.Gestures;

public class UpGesture extends Gesture {

    public UpGesture(float x, float y){
        super(x, y);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleUp(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new UpGesture(_x + dx, _y + dy);
    }
}
