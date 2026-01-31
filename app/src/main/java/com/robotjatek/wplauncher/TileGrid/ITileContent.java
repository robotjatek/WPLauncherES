package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.QuadRenderer;

public interface ITileContent {
    void draw(float[] projMatrix, float[] viewMatrix, QuadRenderer renderer,
              Tile tile, float x, float y, float width, float height);

    void dispose();
    void forceRedraw();
}
