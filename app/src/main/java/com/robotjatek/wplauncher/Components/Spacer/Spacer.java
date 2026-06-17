package com.robotjatek.wplauncher.Components.Spacer;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

/**
 * A simple UI element that takes up space but does not render anything.
 * Useful for adding spacing between other elements in a layout.
 */
public class Spacer implements UIElement {

    private Size<Integer> _size;

    public Spacer(int width, int height) {
        _size = new Size<>(width, height);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        // Nothing to draw, it's just a spacer
    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    public void setSize(int width, int height) {
        _size = new Size<>(width, height);
    }

    @Override
    public void dispose() {
        // Nothing to dispose
    }
}
