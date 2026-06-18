package com.robotjatek.wplauncher.Components.Modal;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.QuadRenderer;

public interface IModal {
    void draw(float delta, float[] projMatrix, QuadRenderer renderer);
    void onResize(int width, int height);
    Size<Integer> getSize();
    void setModalTranslationHeight(float height);
    boolean handleGesture(Gesture gesture);
    void dispose();
}
