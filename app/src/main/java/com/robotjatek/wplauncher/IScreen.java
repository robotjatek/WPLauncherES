package com.robotjatek.wplauncher;

public interface IScreen {
    void draw(float delta, float[] projMatrix, QuadRenderer renderer);
    void onBackPressed();
    void onResize(int width, int height);
    void onTouchStart(float x, float y);
    void onTouchEnd(float x, float y);
    void onTouchMove(float x, float y);
    void dispose();
}
