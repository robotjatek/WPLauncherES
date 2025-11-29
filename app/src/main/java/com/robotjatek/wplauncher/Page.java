package com.robotjatek.wplauncher;

public interface Page {
    void draw(float delta, float[] projMatrix, float[] viewMatrix);

    void touchMove(float y);

    void touchStart(float y);

    void touchEnd(float y);
}

