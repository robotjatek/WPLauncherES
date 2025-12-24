package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.QuadRenderer;

// TODO: AdornerRenderingContext impl + compose into TileGrid
public interface IAdornerRenderingContext {
    float xOf(Adorner adorner);
    float yOf(Adorner adorner);
    float widthOf(Adorner adorner);
    float heightOf(Adorner adorner);
    QuadRenderer getRenderer();
}
