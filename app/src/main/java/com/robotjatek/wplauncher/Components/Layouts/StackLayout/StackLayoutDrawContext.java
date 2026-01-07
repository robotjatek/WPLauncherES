package com.robotjatek.wplauncher.Components.Layouts.StackLayout;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.QuadRenderer;

public class StackLayoutDrawContext implements IDrawContext<UIElement> {

    private final ILayout _layout;

    public StackLayoutDrawContext(StackLayout layout) {
        _layout = layout;
    }

    @Override
    public QuadRenderer getRenderer() {
        return _layout.getRenderer();
    }

    @Override
    public float xOf(UIElement element) {
        return _layout.getLayoutInfo(element).x();
    }

    @Override
    public float yOf(UIElement element) {
        return _layout.getLayoutInfo(element).y();
    }

    @Override
    public float widthOf(UIElement element) {
        return _layout.getWidth();
    }

    @Override
    public float heightOf(UIElement element) {
        return element.measure().height();
    }
}
