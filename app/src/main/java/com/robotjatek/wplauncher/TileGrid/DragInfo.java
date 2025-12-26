package com.robotjatek.wplauncher.TileGrid;

public class DragInfo {
    public float startX = 0f;
    public float startY = 0f;
    public float totalX = 0f;
    public float totalY = 0f;

    public void start(float startX, float startY) {
        this.startX = startX;
        this.startY = startY;
        this.totalX = 0;
        this.totalY = 0;
    }

    public void update(float x, float y) {
        this.totalX = x - startX;
        this.totalY = y - startY;
    }

    public void reset() {
        startX = 0;
        startY = 0;
        totalX = 0;
        totalY = 0;
    }
}
