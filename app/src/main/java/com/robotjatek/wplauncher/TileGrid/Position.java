package com.robotjatek.wplauncher.TileGrid;

public record Position<T>(T x, T y) {
    public static final Position<Float> ZERO = new Position<>(0f, 0f);
}
