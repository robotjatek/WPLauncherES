package com.robotjatek.wplauncher;

public interface Page {
    void draw(float delta, float[] projMatrix, float[] viewMatrix);

    void touchMove(float x, float y);

    void touchStart(float x, float y);

    void touchEnd(float x, float y);

    default boolean isCatchingGestures() {
        return false;
    }

    void onSizeChanged(int width, int height);

    void dispose();
}

