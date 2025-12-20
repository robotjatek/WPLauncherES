package com.robotjatek.wplauncher.AppList;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.TileUtil;

// TODO: icon
// TODO: onclick handler?
public class ListItem {
    private String _label;
    private int _textureId;
    private final int _width;
    private final int _height;
    private final float[] _modelMatrix = new float[16];
    private final IListItemDrawContext _context;

    private boolean _dirty = true;

    public ListItem(String label, int width, int height, IListItemDrawContext context) {
        _label = label;
        _width = width;
        _height = height;
        _textureId = TileUtil.createTextTexture(label, width, height, 0xffffffff, 0xff0000ff);
        _context = context;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        _label = label;
        _dirty = true;
    }

    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, _context.xOf(this), _context.yOf(this), 0);
        Matrix.scaleM(_modelMatrix, 0, _context.widthOf(this), _context.heightOf(this), 0);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        _context.getRenderer().draw(projMatrix, _modelMatrix, _textureId);
    }

    public void update(float delta) {
        // TODO: queue up opengl events into a command list
        if (_dirty) {
            TileUtil.deleteTexture(_textureId);
            _textureId = TileUtil.createTextTexture(_label, _width, _height, 0xffffffff, 0xff0000ff);
            _dirty = false;
        }
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        _textureId = -1;
    }
}