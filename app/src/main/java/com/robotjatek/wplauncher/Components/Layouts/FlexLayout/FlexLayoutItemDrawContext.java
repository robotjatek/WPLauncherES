package com.robotjatek.wplauncher.Components.Layouts.FlexLayout;

import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;

public class FlexLayoutItemDrawContext implements IDrawContext<UIElement> {

    private final FlexLayout _layout;

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
        if (element instanceof FlexLayout childLayout) {
            if (_layout.getDirection() == FlexLayout.Direction.COLUMN &&
                    _layout.getAlign() == FlexLayout.AlignItems.STRETCH) {
                return _layout.getWidth();
            }
            return childLayout.getWidth();
        }

        return element.measure().width();
    }

    @Override
    public float heightOf(UIElement element) {
        if (element instanceof FlexLayout childLayout) {
            if (_layout.getAlign() == FlexLayout.AlignItems.STRETCH &&
            _layout.getDirection() == FlexLayout.Direction.ROW) {
                return _layout.getHeight();
            }
            return childLayout.getHeight();
        }
        return element.measure().height();
    }
}
