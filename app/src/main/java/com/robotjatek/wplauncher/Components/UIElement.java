package com.robotjatek.wplauncher.Components;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public interface UIElement {
     void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer); // TODO: pass delta?
     Size<Integer> measure();
     void dispose();
     void onTap();
}