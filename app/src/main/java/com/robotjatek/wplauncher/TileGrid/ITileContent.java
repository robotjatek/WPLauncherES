package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.QuadRenderer;

public interface ITileContent {
    void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer,
              Position<Float> position, Size<Integer> size);
    void forceRedraw();
    boolean hasContent();
    void setParent(Tile parent);
    void dispose();
}
