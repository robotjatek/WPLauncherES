package com.robotjatek.wplauncher;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Gestures.IGestureHandler;

public interface Page extends IGestureHandler {
    void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer);

    default boolean isCatchingGestures() {
        return false;
    }

    void onSizeChanged(int width, int height);
    
	void resetState();

    boolean handleGesture(Gesture gesture);

    void dispose();
}

