package com.robotjatek.wplauncher;

public class Tile {
    public int x;
    public int y;
    public int colSpan;
    public int rowSpan;
    public String title;
    public int textureId;

    public Tile(int x, int y, int colSpan, int rowSpan, String title) {
        this.x = x;
        this.y = y;
        this.colSpan = colSpan;
        this.rowSpan = rowSpan;
        this.title = title;
        this.textureId = TileUtil.createTextTexture(title, 512, 512, 0xffffffff);
    }
}
