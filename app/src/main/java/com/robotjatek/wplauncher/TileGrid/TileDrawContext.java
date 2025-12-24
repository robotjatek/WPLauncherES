package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public class TileDrawContext implements IDrawContext<Tile> {

    private final float PAGE_PADDING_PX;
    private final float TILE_GAP_PX;
    private float _tileSizePx;
    private final QuadRenderer _renderer;

    public TileDrawContext(float padding, float gap, float tileSize, QuadRenderer renderer) {
        PAGE_PADDING_PX = padding;
        TILE_GAP_PX = gap;
        _tileSizePx = tileSize;
        _renderer = renderer;
    }

    public void onResize(float tileSize) {
        _tileSizePx = tileSize;
    }

    @Override
    public float xOf(Tile t) {
        return PAGE_PADDING_PX + t.x * (_tileSizePx + TILE_GAP_PX);
    }

    @Override
    public float yOf(Tile t) {
        return PAGE_PADDING_PX + t.y * (_tileSizePx + TILE_GAP_PX);
    }

    @Override
    public float widthOf(Tile t) {
        return t.colSpan * _tileSizePx + (t.colSpan - 1) * TILE_GAP_PX;
    }

    @Override
    public float heightOf(Tile t) {
        return t.rowSpan * _tileSizePx + (t.rowSpan - 1) * TILE_GAP_PX;
    }

    @Override
    public QuadRenderer getRenderer() {
        return _renderer;
    }
}
