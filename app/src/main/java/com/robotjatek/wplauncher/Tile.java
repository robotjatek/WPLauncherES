package com.robotjatek.wplauncher;

public class Tile {
    public int x;
    public int y;
    public int colSpan;
    public int rowSpan;

    public Tile(int x, int y, int colSpan, int rowSpan) {
        this.x = x;
        this.y = y;
        this.colSpan = colSpan;
        this.rowSpan = rowSpan;
    }
}
