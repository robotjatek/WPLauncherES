package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.QuadRenderer;

public interface ITileContent {
    void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer,
              Tile tile, Position<Float> position, Size<Float> size);

    void dispose();
    void forceRedraw();
}
