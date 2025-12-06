package com.robotjatek.wplauncher.StartPage.States;

public interface IGestureState {
    void enter();
    void exit();
    void handleTouchStart(float x, float y);
    void handleTouchEnd(float x, float y);
    void handleMove(float x, float y);
    void update(float delta);
}
