package com.robotjatek.wplauncher;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Gestures.IGestureHandler;

public interface IState extends IGestureHandler {
    void enter();
    void exit();
    default boolean handleGesture(Gesture gesture) {
        return gesture.dispatch(this);
    }
    void update(float delta);
}
