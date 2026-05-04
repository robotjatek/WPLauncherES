package com.robotjatek.wplauncher.Components.Layouts;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public interface ILayout extends UIElement {
    IDrawContext<UIElement> getContext();
    void onResize(int width, int height);
    int getWidth();
    int getHeight();
    LayoutInfo getLayoutInfo(UIElement item);
    void draw(float delta, float[] proj, float[] view, QuadRenderer renderer, Position<Float> position, Size<Integer> size);
    UIElement findChildAt(float x, float y);
    default boolean handleGesture(Gesture gesture) {
        var child = findChildAt(gesture.getX(), gesture.getY());
        if (child != null) {
            var childX = getContext().xOf(child);
            var childY = getContext().yOf(child);
            var offsetGesture = gesture.copyWithOffset(-childX, -childY);
            if (child.handleGesture(offsetGesture)) {
                return true;
            }
        }

        return gesture.dispatch(this);
    }
    void dispose();
}
