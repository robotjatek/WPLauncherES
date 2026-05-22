package com.robotjatek.wplauncher.Components.ContextMenu;

import com.robotjatek.wplauncher.IDrawContext;

public class ContextMenuDrawContext<T> implements IDrawContext<ContextMenu<T>> {
    private final IContextMenuParent _parentList;

    public ContextMenuDrawContext(IContextMenuParent list) {
        _parentList = list;
    }

    @Override
    public float xOf(ContextMenu<T> menu) {
        // confine to screen
        return Math.clamp(menu._position.x(), 0, _parentList.getSize().width() - this.widthOf(menu));
    }

    @Override
    public float yOf(ContextMenu<T> menu) {
        // confine to screen
        return Math.clamp(menu._position.y(), 0, _parentList.getSize().height() - _parentList.getBottomMargin() - _parentList.getTopMargin() - this.heightOf(menu));
    }

    @Override
    public float widthOf(ContextMenu<T> menu) {
        return 400;
    }

    @Override
    public float heightOf(ContextMenu<T> menu) {
        return menu.calculateHeight();
    }
}
