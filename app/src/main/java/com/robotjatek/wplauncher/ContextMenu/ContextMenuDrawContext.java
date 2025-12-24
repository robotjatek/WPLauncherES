package com.robotjatek.wplauncher.ContextMenu;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public class ContextMenuDrawContext implements IDrawContext<ContextMenu> {

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
    public float xOf(ContextMenu menu) {
        // confine to screen
        return Math.clamp(menu.position.x(), 0, _listWidth - this.widthOf(menu));
    }

    @Override
    public float yOf(ContextMenu menu) {
        // confine to screen
        return Math.clamp(menu.position.y(), 0, _viewPortHeight - this.heightOf(menu));
    }

    @Override
    public float widthOf(ContextMenu menu) {
        return 400;
    }

    @Override
    public float heightOf(ContextMenu menu) {
        return menu.calculateHeight();
    }

    @Override
    public QuadRenderer getRenderer() {
        return _renderer;
    }
}
