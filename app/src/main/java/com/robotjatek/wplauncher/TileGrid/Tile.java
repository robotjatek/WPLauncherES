package com.robotjatek.wplauncher.TileGrid;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;

public class Tile {
    private Position<Integer> _position;
    private Size<Integer> _size;
    public String title;
    private final App _app;
    public int bgColor;
    private final DragInfo _dragInfo = new DragInfo();
    private final ITileContent _content;

    public Tile(Position<Integer> position, Size<Integer> size, String title, App app, int bgColor, ITileContent content) {
        _position = position;
        _size = size;
        this.title = title;
        this.bgColor = bgColor;
        _app = app;
        _content = content;
    }

    /**
     * Draw matrix with an offset of its original position. Scaling can be applied
     */
    public void drawWithOffsetScaled(float[] projMatrix, float[] viewMatrix, float scale, Position<Float> offset, IDrawContext<Tile> drawContext) {
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

    public Size<Integer> getSize() {
        return _size;
    }

    public void setSize(Size<Integer> size) {
        _size = size;
        _content.forceRedraw();
    }

    public Position<Integer> getPosition() {
        return _position;
    }

    public void setPosition(Position<Integer> position) {
        _position = position;
    }

    public void dispose() {
        _content.dispose();
    }
}
