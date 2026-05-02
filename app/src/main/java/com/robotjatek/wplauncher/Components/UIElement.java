package com.robotjatek.wplauncher.Components;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public interface UIElement {
     void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer);
     Size<Integer> measure();
     void onTap();
     void dispose();
}