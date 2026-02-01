package com.robotjatek.wplauncher.Components.Layouts.FlexLayout;

import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;

public class FlexLayoutItemDrawContext implements IDrawContext<UIElement> {

    private final ILayout _layout;

    public FlexLayoutItemDrawContext(FlexLayout layout) {
        _layout = layout;
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
        // TODO: support layouts as elements
        return element.measure().width();
    }

    @Override
    public float heightOf(UIElement element) {
        // TODO: support layouts as elements
        return element.measure().height();
    }
}
