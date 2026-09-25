package com.robotjatek.wplauncher.Components;

public interface ITouchable {
    /**
     * Called on triggering the touch animation
     */
    void onPress();

    /**
     * Called on triggering the release animation
     */
    void onRelease();

    /**
     * Called after the animation sequence is finished
     */
    void onAction();
}
