package com.robotjatek.wplauncher.Gestures;

public interface IGestureHandler {
    default boolean handleTap(TapGesture gesture) {
        return false;
    }

    default boolean handleLongPress(LongPressGesture gesture) {
        return false;
    }
}
