package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.LauncherRenderer;
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
        if (t.getVisualWidth() > 0) {
            return t.getVisualWidth();
        }
        return calculateWidth(t.getSize());
    }

    @Override
    public float heightOf(Tile t) {
        if (t.getVisualHeight() > 0) {
            return t.getVisualHeight();
        }
        return calculateHeight(t.getSize());
    }

    public float logicalHeightOf(Tile t) {
        return calculateHeight(t.getSize());
    }

    public boolean isVisible(Tile tile) {
        var scrollOffset = _parent.getScroll().getScrollOffset();
        var containerHeight = _parent.getPageHeight();
        var y = yOf(tile);
        var height = heightOf(tile);

        return !(y + height + PAGE_PADDING_PX + LauncherRenderer.SCREEN_DATA.topInset < -scrollOffset || y > -scrollOffset + containerHeight);
    }

    public ScrollController getScroll() {
        return _parent.getScroll();
    }

    public float calculateWidth(Size<Integer> size) {
        return size.width() * _tileSizePx + (size.width() - 1) * TILE_GAP_PX;
    }

    public float calculateHeight(Size<Integer> size) {
        return size.height() * _tileSizePx + (size.height() - 1) * TILE_GAP_PX;
    }
}
