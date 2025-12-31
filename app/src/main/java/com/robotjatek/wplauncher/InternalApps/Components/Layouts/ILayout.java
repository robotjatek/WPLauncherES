package com.robotjatek.wplauncher.InternalApps.Components.Layouts;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.InternalApps.Components.UIElement;
import com.robotjatek.wplauncher.QuadRenderer;

import java.util.List;

public interface ILayout {
    IDrawContext<UIElement> getContext();
    QuadRenderer getRenderer();
    void onResize(int width, int height);
    int getWidth();
    int getHeight();
    List<UIElement> getChildren();
    LayoutInfo getLayoutInfo(UIElement item);
    void draw(float delta, float[] proj);

}
