package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.IDrawContext;

public class Tile {

    public int x;
    public int y;
    public int colSpan;
    public int rowSpan;
    public String title;
    private final App _app;
    public int bgColor;
    private final DragInfo _dragInfo = new DragInfo();
    private final ITileContent _content;

    public Tile(int x, int y, int colSpan, int rowSpan, String title, App app, int bgColor, ITileContent content) {
        this.x = x;
        this.y = y;
        this.colSpan = colSpan;
        this.rowSpan = rowSpan;
        this.title = title;
        this.bgColor = bgColor;
        _app = app;
        _content = content;
    }

    /**
     * Draw matrix with an offset of its original position. Scaling can be applied
     */
    public void drawWithOffsetScaled(float[] projMatrix, float[] viewMatrix, float scale, Position offset, IDrawContext<Tile> drawContext) {
        var width = drawContext.widthOf(this) * scale;
        var height = drawContext.heightOf(this) * scale;
        var xDiff = (width - drawContext.widthOf(this)) / 2; // correction for the scaling
        var yDiff = (height - drawContext.heightOf(this)) / 2; // correction for the scaling

        var correctedX = drawContext.xOf(this) + offset.x() - xDiff; // x corrected by the scaling and the offset
        var correctedY = drawContext.yOf(this) + offset.y() - yDiff; // y corrected by the scaling and the offset

        _content.draw(projMatrix, viewMatrix, drawContext, this, correctedX, correctedY, width, height);
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

    public App getApp() {
        return _app;
    }

    public void setBgColor(int color) {
        bgColor = color;
        _content.forceRedraw();
    }

    public DragInfo getDragInfo() {
        return _dragInfo;
    }

    public void dispose() {
        _content.dispose();
    }
}
