package com.robotjatek.wplauncher;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

public class TileGrid implements Page {

    private final float[] scrollMatrix = new float[16]; // Stores the state of the scroll position transformation
    private final float[] modelMatrix = new float[16]; // Reused model matrix for the individual tiles
    private Color color = new Color(1, 0, 0); // TODO: Get tile color from an options service

    private final Tile tile1 = new Tile(0, 0, 2, 2); // 2x2 tile
    private final Tile tile2 = new Tile(0, 2, 1, 1); // 1x1 tile
    private final Tile tile3 = new Tile(0, 4, 4, 2); // Wide tile
    private final Tile tile4 = new Tile(0, 8, 4, 2); // Wide tile
    private final Tile tile5 = new Tile(0, 20, 4, 4); // 4x4 large tile far down
    private final List<Tile> tiles = new ArrayList<>(List.of(tile1, tile2, tile3, tile4, tile5));

    private static final int COLUMNS = 4;
    private static final float TOP_MARGIN = 128;
    private static final float PAGE_PADDING_PX = 24;
    private static final float TILE_GAP_PX = 18;

    private final QuadRenderer renderer = new QuadRenderer();

    private float tileSizePx;
    private float scrollOffset = TOP_MARGIN;
    private float scrollStartOffset = TOP_MARGIN;
    private float touchStartY = 0;

    private float viewportHeight = 0;

    public TileGrid() {
        Matrix.setIdentityM(scrollMatrix, 0);
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        Matrix.setIdentityM(scrollMatrix, 0);
        Matrix.translateM(scrollMatrix, 0, 0, scrollOffset, 0);

        for (var t : tiles) {
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, tileX(t), tileY(t), 0);
            Matrix.scaleM(modelMatrix, 0, tileWidth(t), tileHeight(t), 1);

            Matrix.multiplyMM(modelMatrix, 0, scrollMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelMatrix, 0, viewMatrix, 0, modelMatrix, 0);

            renderer.setColor(color);
            renderer.draw(projMatrix, modelMatrix);
        }
    }

    public void onSizeChanged(int w, int h) {
        float usableWidth = w - (2 * PAGE_PADDING_PX) - ((COLUMNS - 1) * TILE_GAP_PX);
        tileSizePx = usableWidth / COLUMNS;
        viewportHeight = h;
    }

    public void pinTile(Tile t) {
        this.tiles.add(t);
    }

    public void touchStart(float y) {
        touchStartY = y;
        scrollStartOffset = scrollOffset;
    }

    @Override
    public void touchMove(float y) {
        float dy = y - touchStartY;
        scrollOffset = scrollStartOffset + dy;
        clampScroll();
    }

    private void clampScroll() {
        float contentHeight = getContentHeight();
        float minScroll = Math.min(TOP_MARGIN, viewportHeight - contentHeight);
        scrollOffset = Math.max(minScroll, Math.min(scrollOffset, TOP_MARGIN));
    }

    private float tileX(Tile t) {
        return PAGE_PADDING_PX + t.x * (tileSizePx + TILE_GAP_PX);
    }

    private float tileY(Tile t) {
        return PAGE_PADDING_PX + t.y * (tileSizePx + TILE_GAP_PX);
    }

    private float tileWidth(Tile t) {
        return t.colSpan * tileSizePx + (t.colSpan - 1) * TILE_GAP_PX;
    }

    private float tileHeight(Tile t) {
        return t.rowSpan * tileSizePx + (t.rowSpan - 1) * TILE_GAP_PX;
    }

    private float getContentHeight() {
        float max = 0;
        for (var t : tiles) {
            float bottom = tileY(t) + tileHeight(t) + PAGE_PADDING_PX;
            if (bottom > max) max = bottom;
        }
        return max;
    }
}
