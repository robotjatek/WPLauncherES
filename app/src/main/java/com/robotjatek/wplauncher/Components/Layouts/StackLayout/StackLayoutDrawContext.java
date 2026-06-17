package com.robotjatek.wplauncher.Components.Layouts.StackLayout;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.Components.UIElement;

public class StackLayoutDrawContext implements IDrawContext<UIElement> {

    private final StackLayout _layout;

    public StackLayoutDrawContext(StackLayout layout) {
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
        if (_layout.getOrientation() == StackLayout.Orientation.VERTICAL) {
            return _layout.getWidth() - _layout.getPadding() * 2;
        } else {
            return element.measure().width();
        }
    }

    @Override
    public float heightOf(UIElement element) {
        if (_layout.getOrientation() == StackLayout.Orientation.VERTICAL) {
            return element.measure().height();
        } else {
            return _layout.getHeight() - _layout.getPadding() * 2;
        }
    }
}
