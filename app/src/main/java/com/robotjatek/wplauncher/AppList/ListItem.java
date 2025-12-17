package com.robotjatek.wplauncher.AppList;

import com.robotjatek.wplauncher.TileUtil;

// TODO: icon
// TODO: onclick handler?
class ListItem {
    private String _label;
    private int _textureId;
    private int _width;
    private int _height;

    private boolean _dirty = true;

    public ListItem(String label, int width, int height) {
        _label = label;
        _width = width;
        _height = height;
        _textureId = TileUtil.createTextTexture(label, width, height, 0xffffffff);
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        _label = label;
        _dirty = true;
    }

    public int getTextureId() {
        return _textureId;
    }

    public void update(float delta) {
        // TODO: queue up opengl events into a command list
        if (_dirty) {
            TileUtil.deleteTexture(_textureId);
            _textureId = TileUtil.createTextTexture(_label, _width, _height, 0xffffffff);
            _dirty = false;
        }
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        _textureId = -1;
    }
}