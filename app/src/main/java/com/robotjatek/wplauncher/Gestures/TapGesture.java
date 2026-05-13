package com.robotjatek.wplauncher.Gestures;

import com.robotjatek.wplauncher.IUIContext;

public class TapGesture extends Gesture {
    public TapGesture(float x, float y, IUIContext uiContext) {
        super(x, y, uiContext);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleTap(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new TapGesture(_x + dx, _y + dy, _uiContext);
    }
}
