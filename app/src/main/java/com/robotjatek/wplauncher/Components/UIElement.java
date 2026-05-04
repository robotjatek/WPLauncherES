package com.robotjatek.wplauncher.Components;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Gestures.IGestureHandler;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public interface UIElement extends IGestureHandler {
     void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer); // TODO: pass delta?
     Size<Integer> measure();
     default boolean handleGesture(Gesture gesture) {
          return gesture.dispatch(this);
     }
     void dispose();
}