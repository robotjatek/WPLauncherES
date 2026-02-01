package com.robotjatek.wplauncher.Components.Layouts;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public interface ILayout {
    IDrawContext<UIElement> getContext();
    void onResize(int width, int height);
    int getWidth();
    int getHeight();
    LayoutInfo getLayoutInfo(UIElement item);
    void draw(float delta, float[] proj, float[] view, QuadRenderer renderer, Position<Float> position);
    void onTouchStart(float x, float y);
    void onTouchEnd(float x, float y);
    void onTouchMove(float x, float y);
    void dispose();
}
