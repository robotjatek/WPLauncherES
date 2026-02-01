package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.IDrawContext;

public class AdornerDrawContext<T extends IAdornedTileContainer> implements IDrawContext<Adorner> {

    private final TileDrawContext _tileDrawContext;
    private final T _tileContainer;

    public AdornerDrawContext(TileDrawContext tileDrawContext, T tileContainer) {
        _tileDrawContext = tileDrawContext;
        _tileContainer = tileContainer;
    }

    @Override
    public float xOf(Adorner adorner) {
        var selectedTile = _tileContainer.getAdornedTile();
        if (adorner == null || selectedTile == null) {
            throw new RuntimeException();
        }

        return
                adorner.getRelativePosition().x() * _tileDrawContext.widthOf(selectedTile)
                + _tileDrawContext.xOf(selectedTile)
                - widthOf(adorner) / 2 + selectedTile.getDragInfo().totalX;
    }

    @Override
    public float yOf(Adorner adorner) {
        var selectedTile = _tileContainer.getAdornedTile();
        if (adorner == null || selectedTile == null) {
            throw new RuntimeException();
        }
        return _tileDrawContext.yOf(selectedTile) +
                (_tileDrawContext.heightOf(selectedTile) * adorner.getRelativePosition().y())
                - heightOf(adorner) / 2
                + selectedTile.getDragInfo().totalY;
    }

    @Override
    public float widthOf(Adorner adorner) {
        return 96;
    }

    @Override
    public float heightOf(Adorner adorner) {
        return 96;
    }
}
