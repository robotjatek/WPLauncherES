package com.robotjatek.wplauncher.Components.ContextMenu;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public class ContextMenuDrawContext<T> implements IDrawContext<ContextMenu<T>> {

    private int _listWidth;
    private int _viewPortHeight;
    private final QuadRenderer _renderer;

    public ContextMenuDrawContext(int listWidth, int viewPortHeight, QuadRenderer renderer) {
        _listWidth = listWidth;
        _viewPortHeight = viewPortHeight;
        _renderer = renderer;
    }

    public void onResize(int listWidth, int viewPortHeight) {
        _listWidth = listWidth;
        _viewPortHeight = viewPortHeight;
    }

    @Override
    public float xOf(ContextMenu<T> menu) {
        // confine to screen
        return Math.clamp(menu._position.x(), 0, _listWidth - this.widthOf(menu));
    }

    @Override
    public float yOf(ContextMenu<T> menu) {
        // confine to screen
        return Math.clamp(menu._position.y(), 0, _viewPortHeight - this.heightOf(menu));
    }

    @Override
    public float widthOf(ContextMenu<T> menu) {
        return 400;
    }

    @Override
    public float heightOf(ContextMenu<T> menu) {
        return menu.calculateHeight();
    }

    @Override
    public QuadRenderer getRenderer() {
        return _renderer;
    }
}
