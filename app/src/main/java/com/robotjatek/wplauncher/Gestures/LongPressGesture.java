package com.robotjatek.wplauncher.Gestures;

import com.robotjatek.wplauncher.IUIContext;

public class LongPressGesture extends Gesture {

    public LongPressGesture(float x, float y, IUIContext uiContext) {
        super(x, y, uiContext);
    }

    @Override
    public boolean dispatch(IGestureHandler handler) {
        return handler.handleLongPress(this);
    }

    @Override
    public Gesture copyWithOffset(float dx, float dy) {
        return new LongPressGesture(_x + dx, _y + dy, _uiContext);
    }
}
