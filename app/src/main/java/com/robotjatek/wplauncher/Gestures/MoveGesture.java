package com.robotjatek.wplauncher.Gestures;

import com.robotjatek.wplauncher.IUIContext;

public class MoveGesture extends Gesture {

    public MoveGesture(float x, float y, IUIContext uiContext) {
        super(x, y, uiContext);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleMove(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new MoveGesture(_x + dx, _y + dy, _uiContext);
    }
}
