package com.robotjatek.wplauncher.TileGrid;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.VerticalAlign;
import com.robotjatek.wplauncher.TileUtil;

public class Tile {
    private static final int TEXTURE_UNIT_PX = 256;
    private static final int ICON_SIZE_PX = 512;

    public int x;
    public int y;
    public int colSpan;
    public int rowSpan;
    public String title;
    private int _textureId = -1;
    private int _iconTextureId = -1;
    private final float[] _modelMatrix = new float[16];
    private final App _app;
    private boolean _dirty = true;

    private static final Position NO_OFFSET = new Position(0, 0);

    public Tile(int x, int y, int colSpan, int rowSpan, String title, App app) {
        this.x = x;
        this.y = y;
        this.colSpan = colSpan;
        this.rowSpan = rowSpan;
        this.title = title;
        _app = app;
    }

    /**
     * Draw the tile normally on its determined position
     */
    public void draw(float[] projMatrix, float[] viewMatrix, TileDrawContext drawContext) {
        drawWithOffsetScaled(projMatrix, viewMatrix, 1.0f, NO_OFFSET, drawContext);
    }

    /**
     * Draw matrix with an offset of its original position. Scaling can be applied
     */
    public void drawWithOffsetScaled(float[] projMatrix, float[] viewMatrix, float scale, Position offset, TileDrawContext drawContext) {
        if (_dirty) {
            // TODO: move this to a command buffer and run before rendering a frame
            TileUtil.deleteTexture(_textureId);
            TileUtil.deleteTexture(_iconTextureId);
            var bgColor = 0xff1a1a2e; // TODO: bg color from config service
            _textureId = TileUtil.createTextTexture(title, TEXTURE_UNIT_PX * colSpan, TEXTURE_UNIT_PX * rowSpan, 0xffffffff, bgColor, VerticalAlign.BOTTOM);
            _iconTextureId = BitmapUtil.createTextureFromDrawable(_app.icon(), ICON_SIZE_PX, ICON_SIZE_PX);
            _dirty = false;
        }
        var width = drawContext.tileWidth(this) * scale;
        var height = drawContext.tileHeight(this) * scale;
        var xDiff = (width - drawContext.tileWidth(this)) / 2; // correction for the scaling
        var yDiff = (height - drawContext.tileHeight(this)) / 2; // correction for the scaling

        var correctedX = drawContext.tileX(this) + offset.x() - xDiff; // x corrected by the scaling and the offset
        var correctedY = drawContext.tileY(this) + offset.y() - yDiff; // y corrected by the scaling and the offset

        // Background
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, correctedX, correctedY, 0f);
        Matrix.scaleM(_modelMatrix, 0, width, height, 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        drawContext.getRenderer().draw(projMatrix, _modelMatrix, _textureId);

        // Icon
        // Center icon, keep aspect ratio on wide tiles
        var iconSize = Math.min(width, height) / 2;
        var iconX = correctedX + (width - iconSize) / 2;
        var iconY = correctedY + (height - iconSize) / 2;
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, iconX, iconY, 0f);
        Matrix.scaleM(_modelMatrix, 0, iconSize, iconSize, 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        drawContext.getRenderer().draw(projMatrix, _modelMatrix, _iconTextureId);
    }

    public void onTap() {
        if (_app != null) {
            _app.action().run();
        }
    }

    public String getPackageName() {
        if (_app == null) {
            return "";
        }
        return _app.packageName();
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        TileUtil.deleteTexture(_iconTextureId);
        _textureId = -1;
    }
}
