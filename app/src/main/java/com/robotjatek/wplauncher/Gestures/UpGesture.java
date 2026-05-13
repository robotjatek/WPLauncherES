package com.robotjatek.wplauncher.Gestures;

import com.robotjatek.wplauncher.IUIContext;

public class UpGesture extends Gesture {

    public UpGesture(float x, float y, IUIContext uiContext) {
        super(x, y, uiContext);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleUp(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new UpGesture(_x + dx, _y + dy, _uiContext);
    }
}
