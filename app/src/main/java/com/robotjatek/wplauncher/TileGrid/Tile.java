package com.robotjatek.wplauncher.TileGrid;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.TileUtil;

public class Tile {
    private final TileDrawContext _drawContext;
    private static final int TEXTURE_UNIT_PX = 256;

    public int x;
    public int y;
    public int colSpan;
    public int rowSpan;
    public String title;
    private int textureId;
    private final float[] _modelMatrix = new float[16];

    private static final Position NO_OFFSET = new Position(0, 0);

    public Tile(int x, int y, int colSpan, int rowSpan, String title, TileDrawContext drawContext) {
        _drawContext = drawContext;
        this.x = x;
        this.y = y;
        this.colSpan = colSpan;
        this.rowSpan = rowSpan;
        this.title = title;
        this.textureId = TileUtil.createTextTexture(title, TEXTURE_UNIT_PX * colSpan, TEXTURE_UNIT_PX * rowSpan, 0xffffffff);
    }

    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        drawScaled(delta, projMatrix, viewMatrix, 1.0f, NO_OFFSET);
    }

    public void drawScaled(float delta, float[] projMatrix, float[] viewMatrix, float scale, Position offset) {
        var width = _drawContext.tileWidth(this) * scale;
        var height = _drawContext.tileHeight(this) * scale;
        var xDiff = (width - _drawContext.tileWidth(this)) / 2;
        var yDiff = (height - _drawContext.tileHeight(this)) / 2;

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0,
                _drawContext.tileX(this) + offset.x() - xDiff,
                _drawContext.tileY(this) + offset.y() - yDiff, 0f);
        Matrix.scaleM(_modelMatrix, 0, width, height, 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        _drawContext.getRenderer().draw(projMatrix, _modelMatrix, textureId);
    }

    public void dispose() {
        TileUtil.deleteTexture(textureId);
        textureId = -1;
    }
}
