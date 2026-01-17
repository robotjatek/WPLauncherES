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

    @Override
    public QuadRenderer getRenderer() {
        return _renderer;
    }
}
