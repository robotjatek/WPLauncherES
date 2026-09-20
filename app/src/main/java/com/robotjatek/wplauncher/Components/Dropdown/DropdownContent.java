package com.robotjatek.wplauncher.Components.Dropdown;

import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

// TODO: this may not needed at all
public class DropdownContent implements UIElement {

    private final Dropdown _parent;
    private Size<Integer> _size;

    public DropdownContent(Dropdown parent) {
        _parent = parent;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {

    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    public void setSize(Size<Integer> size) {
        _size = size;
    }

    @Override
    public void setParent(ILayout parent) {

    }

                          @Override
    public void dispose() {

    }
}
