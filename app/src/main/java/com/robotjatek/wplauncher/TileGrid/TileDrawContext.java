package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.QuadRenderer;

public interface TileDrawContext {

    /**
     * Calculates screen space X position of a tile
     * @param t The tile to measure
     * @return Screen space Y positon
     */
    float tileX(Tile t);

    /**
     * Calculates screen space Y position of a tile
     * @param t The tile to measure
     * @return Screen space Y positon
     */
    float tileY(Tile t);

    float tileWidth(Tile t);

    float tileHeight(Tile t);

    QuadRenderer getRenderer();
}
