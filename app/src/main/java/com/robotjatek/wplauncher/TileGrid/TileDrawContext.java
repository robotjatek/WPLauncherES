package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.IDrawContext;

public class TileDrawContext implements IDrawContext<Tile> {

    private final float PAGE_PADDING_PX;
    private final float TILE_GAP_PX;
    private float _tileSizePx;

    public TileDrawContext(float padding, float gap, float tileSize) {
        PAGE_PADDING_PX = padding;
        TILE_GAP_PX = gap;
        _tileSizePx = tileSize;
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
}
