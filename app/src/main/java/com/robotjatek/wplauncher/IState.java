package com.robotjatek.wplauncher;

public interface IState {
    void enter();
    void exit();
    void handleTouchStart(float x, float y);
    void handleTouchEnd(float x, float y);
    void handleMove(float x, float y);
    void update(float delta);
}
