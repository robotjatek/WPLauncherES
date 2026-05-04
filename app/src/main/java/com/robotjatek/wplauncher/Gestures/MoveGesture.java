package com.robotjatek.wplauncher.Gestures;

public class MoveGesture extends Gesture {

    public MoveGesture(float x, float y) {
        super(x, y);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleMove(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new MoveGesture(_x + dx, _y + dy);
    }
}
