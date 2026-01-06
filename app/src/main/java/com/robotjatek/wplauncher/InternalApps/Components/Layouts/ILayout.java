package com.robotjatek.wplauncher.InternalApps.Components.Layouts;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.InternalApps.Components.UIElement;
import com.robotjatek.wplauncher.QuadRenderer;

public interface ILayout {
    IDrawContext<UIElement> getContext();
    QuadRenderer getRenderer();
    void onResize(int width, int height);
    int getWidth();
    int getHeight();
    LayoutInfo getLayoutInfo(UIElement item);
    void draw(float delta, float[] proj);
    void onTouchStart(float x, float y);
    void onTouchEnd(float x, float y);
    void onTouchMove(float x, float y);
}
