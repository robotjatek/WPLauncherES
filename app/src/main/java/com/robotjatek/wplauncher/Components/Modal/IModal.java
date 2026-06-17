package com.robotjatek.wplauncher.Components.Modal;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.QuadRenderer;

public interface IModal {
    void draw(float delta, float[] projMatrix, QuadRenderer renderer);
    void onResize(int width, int height);
    boolean handleGesture(Gesture gesture);
    void dispose();
}
