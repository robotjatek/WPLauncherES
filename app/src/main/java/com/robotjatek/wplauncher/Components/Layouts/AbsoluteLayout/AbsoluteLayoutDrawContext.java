package com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout;

import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;

// Simple draw context that just returns stored positions
public class AbsoluteLayoutDrawContext implements IDrawContext<UIElement> {

    private final AbsoluteLayout _layout;

    public AbsoluteLayoutDrawContext(AbsoluteLayout layout) {
        _layout = layout;
    }

    @Override
    public float xOf(UIElement element) {
        for (var child : _layout.getPositionedElements()) {
            if (child._element == element) return child._position.x();
        }
        return 0;
    }

    @Override
    public float yOf(UIElement element) {
        for (var child : _layout.getPositionedElements()) {
            if (child._element == element) return child._position.y();
        }
        return 0;
    }

    @Override
    public float widthOf(UIElement element) {
        return element.measure().width();
    }

    @Override
    public float heightOf(UIElement element) {
        return element.measure().height();
    }
}
