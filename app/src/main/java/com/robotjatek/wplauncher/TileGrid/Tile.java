package com.robotjatek.wplauncher.TileGrid;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.TileUtil;

public class Tile {
    private static final int TEXTURE_UNIT_PX = 256;

    public int x;
    public int y;
    public int colSpan;
    public int rowSpan;
    public String title;
    private int _textureId = -1;
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

    public void draw(float delta, float[] projMatrix, float[] viewMatrix, TileDrawContext drawContext) {
        drawScaled(delta, projMatrix, viewMatrix, 1.0f, NO_OFFSET, drawContext);
    }

    public void drawScaled(float delta, float[] projMatrix, float[] viewMatrix, float scale, Position offset, TileDrawContext drawContext) {
        if (_dirty) {
            _textureId = TileUtil.createTextTexture(title, TEXTURE_UNIT_PX * colSpan, TEXTURE_UNIT_PX * rowSpan, 0xffffffff, 0xff0000ff); // TODO: move this to a command buffer and run before rendering a frame
            _dirty = false;
        }
        var width = drawContext.tileWidth(this) * scale;
        var height = drawContext.tileHeight(this) * scale;
        var xDiff = (width - drawContext.tileWidth(this)) / 2;
        var yDiff = (height - drawContext.tileHeight(this)) / 2;

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0,
                drawContext.tileX(this) + offset.x() - xDiff,
                drawContext.tileY(this) + offset.y() - yDiff, 0f);
        Matrix.scaleM(_modelMatrix, 0, width, height, 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        drawContext.getRenderer().draw(projMatrix, _modelMatrix, _textureId);
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
        _textureId = -1;
    }
}
