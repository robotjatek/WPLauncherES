package com.robotjatek.wplauncher.AppList;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.TileUtil;

// TODO: icon
public class ListItem<T> {
    private String _label;
    private int _textureId;
    private final int _width;
    private final int _height;
    private final float[] _modelMatrix = new float[16];
    private final IListItemDrawContext<T> _context;
    private final Runnable _onTap;
    private boolean _dirty = true;
    private final T _payload;

    public ListItem(String label, int width, int height, IListItemDrawContext<T> context, Runnable onTap, T payload) {
        _label = label;
        _width = width;
        _height = height;
        _textureId = TileUtil.createTextTexture(label, width, height, 0xffffffff, 0xff0000ff);
        _context = context;
        _onTap = onTap;
        _payload = payload;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        _label = label;
        _dirty = true;
    }

    public T getPayload() {
        return _payload;
    }

    public void draw(float[] projMatrix, float[] viewMatrix) {
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, _context.xOf(this), _context.yOf(this), 0);
        Matrix.scaleM(_modelMatrix, 0, _context.widthOf(this), _context.heightOf(this), 0);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        _context.getRenderer().draw(projMatrix, _modelMatrix, _textureId);
    }

    public void update() {
        // TODO: queue up opengl events into a command list
        if (_dirty) {
            TileUtil.deleteTexture(_textureId);
            _textureId = TileUtil.createTextTexture(_label, _width, _height, 0xffffffff, 0xff0000ff);
            _dirty = false;
        }
    }

    public void onTap() {
        if (_onTap != null) {
            _onTap.run();
        }
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        _textureId = -1;
    }
}