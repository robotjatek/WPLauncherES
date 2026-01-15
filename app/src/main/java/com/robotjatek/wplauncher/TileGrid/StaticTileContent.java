package com.robotjatek.wplauncher.TileGrid;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

public class StaticTileContent implements ITileContent {
    private static final int TEXTURE_UNIT_PX = 256;
    private static final int ICON_SIZE_PX = 512;
    private final float[] _modelMatrix = new float[16];
    private int _textureId = -1;
    private int _iconTextureId = -1;
    private boolean _dirty = true;

    @Override
    public void draw(float[] projMatrix, float[] viewMatrix, IDrawContext<Tile> drawContext, Tile tile, float x, float y, float width, float height) {
        if (_dirty) {
            // TODO: move this to a command buffer and run before rendering a frame
            TileUtil.deleteTexture(_textureId);
            TileUtil.deleteTexture(_iconTextureId);
            _textureId = TileUtil.createTextTexture(tile.title, TEXTURE_UNIT_PX * tile.colSpan,
                    TEXTURE_UNIT_PX * tile.rowSpan, 48, Typeface.BOLD, Colors.WHITE, tile.bgColor, HorizontalAlign.LEFT, VerticalAlign.BOTTOM);
            _iconTextureId = BitmapUtil.createTextureFromDrawable(tile.getApp().icon(), ICON_SIZE_PX, ICON_SIZE_PX);
            _dirty = false;
        }

        drawBackground(projMatrix, viewMatrix, drawContext, x, y, width, height);
        drawIcon(projMatrix, viewMatrix, drawContext, width, height, x, y);
    }

    private void drawIcon(float[] projMatrix, float[] viewMatrix, IDrawContext<Tile> drawContext, float width, float height, float correctedX, float correctedY) {
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

    private void drawBackground(float[] projMatrix, float[] viewMatrix, IDrawContext<Tile> drawContext, float correctedX, float correctedY, float width, float height) {
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, correctedX, correctedY, 0f);
        Matrix.scaleM(_modelMatrix, 0, width, height, 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        drawContext.getRenderer().draw(projMatrix, _modelMatrix, _textureId);
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        TileUtil.deleteTexture(_iconTextureId);
        _textureId = -1;
        _iconTextureId = -1;
    }

    @Override
    public void forceRedraw() {
        _dirty = true;
    }
}
