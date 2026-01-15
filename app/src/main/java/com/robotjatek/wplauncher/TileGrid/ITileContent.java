package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.IDrawContext;

public interface ITileContent {
    void draw(float[] projMatrix, float[] viewMatrix,
              IDrawContext<Tile> drawContext, Tile tile,
              float x, float y, float width, float height);

    void dispose();
    void forceRedraw();
}
