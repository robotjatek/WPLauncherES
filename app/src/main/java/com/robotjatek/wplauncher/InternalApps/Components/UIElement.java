package com.robotjatek.wplauncher.InternalApps.Components;

import com.robotjatek.wplauncher.InternalApps.Components.Layouts.ILayout;

public interface UIElement {
     void draw(float[] proj, float[] view, ILayout layout);
     Size measure();
     void dispose();
     void onTap();
}