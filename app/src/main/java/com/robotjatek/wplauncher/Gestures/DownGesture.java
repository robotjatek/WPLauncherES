package com.robotjatek.wplauncher.Gestures;

import com.robotjatek.wplauncher.IUIContext;

public class DownGesture extends Gesture {

    public DownGesture(float x, float y, IUIContext uiContext) {
        super(x, y, uiContext);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleDown(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new DownGesture(_x + dx, _y + dy, _uiContext);
    }
}
