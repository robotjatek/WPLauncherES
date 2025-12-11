package com.robotjatek.wplauncher;

public class Tile {
    private static final int TEXTURE_UNIT_PX = 256;

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
        this.textureId = TileUtil.createTextTexture(title, TEXTURE_UNIT_PX * colSpan, TEXTURE_UNIT_PX * rowSpan, 0xffffffff);
    }
}
