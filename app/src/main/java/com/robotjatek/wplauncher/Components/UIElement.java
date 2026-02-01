package com.robotjatek.wplauncher.Components;

import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.QuadRenderer;

public interface UIElement {
     void draw(float[] proj, float[] view, ILayout layout, QuadRenderer renderer);
     Size<Float> measure();
     void dispose();
     void onTap();
}