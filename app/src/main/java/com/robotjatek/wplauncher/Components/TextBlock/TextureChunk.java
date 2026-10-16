package com.robotjatek.wplauncher.Components.TextBlock;

import androidx.annotation.NonNull;

import com.robotjatek.wplauncher.TileUtil;

import java.util.function.Supplier;

public class TextureChunk {
    private Supplier<Integer> _textureIdSupplier;
    private Integer _textureId = null;
    private final int _width;
    private final int _height;
    private final int _xOffset;
    private final int _yOffset;

    TextureChunk(Supplier<Integer> textureIdSupplier, int width, int height, int xOffset, int yOffset) {
        _textureIdSupplier = textureIdSupplier;
        _width = width;
        _height = height;
        _xOffset = xOffset;
        _yOffset = yOffset;
    }

    public Integer getTextureId() {
        if (_textureId == null && _textureIdSupplier != null) {
            _textureId = _textureIdSupplier.get();
            _textureIdSupplier = null;
        }
        return _textureId;
    }

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    public int getXOffset() {
        return _xOffset;
    }

    public int getYOffset() {
        return _yOffset;
    }

    public void dispose() {
        if (_textureId != null) {
            TileUtil.deleteTexture(_textureId);
            _textureId = null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "TextureChunk{" +
                "width=" + _width +
                ", height=" + _height +
                ", xOffset=" + _xOffset +
                ", yOffset=" + _yOffset +
                '}';
    }
}
