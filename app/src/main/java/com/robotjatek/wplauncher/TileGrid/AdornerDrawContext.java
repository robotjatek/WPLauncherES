package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public class AdornerDrawContext<T extends IAdornedTileContainer> implements IDrawContext<Adorner> {

    private final TileDrawContext _tileDrawContext;
    private final QuadRenderer _renderer;
    private final T _tileContainer;

    public AdornerDrawContext(TileDrawContext tileDrawContext, QuadRenderer renderer, T tileContainer) {
        _tileDrawContext = tileDrawContext;
        _renderer = renderer;
        _tileContainer = tileContainer;
    }

    @Override
    public float xOf(Adorner adorner) {
        var selectedTile = _tileContainer.getAdornedTile();
        if (adorner == null || selectedTile == null) {
            throw new RuntimeException();
        }
        return _tileDrawContext.widthOf(selectedTile) + _tileDrawContext.xOf(selectedTile) - widthOf(adorner) / 2;
    }

    @Override
    public float yOf(Adorner adorner) {
        var selectedTile = _tileContainer.getAdornedTile();
        if (adorner == null || selectedTile == null) {
            throw new RuntimeException();
        }
        return _tileDrawContext.yOf(selectedTile) - heightOf(adorner) / 2;
    }

    @Override
    public float widthOf(Adorner adorner) {
        return 96;
    }

    @Override
    public float heightOf(Adorner adorner) {
        return 96;
    }

    @Override
    public QuadRenderer getRenderer() {
        return _renderer;
    }
}
