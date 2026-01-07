package com.robotjatek.wplauncher.Components;

import com.robotjatek.wplauncher.Components.Layouts.ILayout;

public interface UIElement {
     void draw(float[] proj, float[] view, ILayout layout);
     Size measure();
     void dispose();
     void onTap();
}