package com.robotjatek.wplauncher.Components;

public interface ITouchable {
    void onPress();
    void onRelease(boolean fireAction);
  //  void cancelPendingAction();
    //void update(float delta);
}
