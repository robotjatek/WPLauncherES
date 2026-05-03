package com.robotjatek.wplauncher;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Gestures.IGestureHandler;

public interface IScreen extends IGestureHandler {
    void draw(float delta, float[] projMatrix, QuadRenderer renderer);
    void onBackPressed();
    void onResize(int width, int height);
    default boolean handleGesture(Gesture gesture) {
        throw new RuntimeException("Implementáld baszod"); // TODO: remove default implementation!
    }
    void dispose();
}
