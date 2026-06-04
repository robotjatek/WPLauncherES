package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.ScrollController;

public class TileDrawContext implements IDrawContext<Tile> {

    private final float PAGE_PADDING_PX;
    private final float TILE_GAP_PX;
    private float _tileSizePx;
    private final TileGrid _parent;

    public TileDrawContext(float padding, float gap, float tileSize, TileGrid parent) {
        PAGE_PADDING_PX = padding;
        TILE_GAP_PX = gap;
        _tileSizePx = tileSize;
        _parent = parent;
    }

    public void onResize(float tileSize) {
        _tileSizePx = tileSize;
    }

    @Override
    public float xOf(Tile t) {
        return PAGE_PADDING_PX + t.getPosition().x() * (_tileSizePx + TILE_GAP_PX);
    }

    @Override
    public float yOf(Tile t) {
        return PAGE_PADDING_PX + t.getPosition().y() * (_tileSizePx + TILE_GAP_PX);
    }

    @Override
    public float widthOf(Tile t) {
        return t.getSize().width() * _tileSizePx + (t.getSize().width() - 1) * TILE_GAP_PX;
    }

    @Override
    public float heightOf(Tile t) {
        return t.getSize().height() * _tileSizePx + (t.getSize().height() - 1) * TILE_GAP_PX;
    }

    public boolean isVisible(Tile tile) {
        var scrollOffset = _parent.getScroll().getScrollOffset();
        var containerHeight = _parent.getPageHeight();
        var y = yOf(tile);
        var height = heightOf(tile);

        return !(y + height + PAGE_PADDING_PX < -scrollOffset || y > -scrollOffset + containerHeight);
    }

    public ScrollController getScroll() {
        return _parent.getScroll();
    }
}
