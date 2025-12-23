package com.robotjatek.wplauncher.AppList;

import android.graphics.drawable.Drawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.VerticalAlign;
import com.robotjatek.wplauncher.TileUtil;

public class ListItem<T> {
    private String _label;
    private final Drawable _icon;
    private int _iconTextureId;
    private int _textureId;
    private final float[] _modelMatrix = new float[16];
    private final IListItemDrawContext<T> _context;
    private final Runnable _onTap;
    private boolean _dirty = true;
    private final T _payload;
    private final int _labelW;

    public ListItem(String label, Drawable icon, IListItemDrawContext<T> context, Runnable onTap, T payload) {
        _label = label;
        _context = context;
        _onTap = onTap;
        _payload = payload;
        _icon = icon;
        var h = _context.heightOf(this);
        var w = _context.widthOf(this);
        _labelW = (int) (w - h);
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
        var x = _context.xOf(this);
        var y = _context.yOf(this);
        var h = _context.heightOf(this);

        var labelX = x + h;
        // icon
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, h, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        _context.getRenderer().draw(projMatrix, _modelMatrix, _iconTextureId);

        // label
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, labelX, y, 0);
        Matrix.scaleM(_modelMatrix, 0, _labelW, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        _context.getRenderer().draw(projMatrix, _modelMatrix, _textureId);
    }

    public void update() {
        // TODO: queue up opengl events into a command list
        if (_dirty) {
            TileUtil.deleteTexture(_textureId);
            _textureId = TileUtil.createTextTexture(_label, _labelW, (int)_context.heightOf(this), 0xffffffff, 0x000000ff, VerticalAlign.CENTER);
            TileUtil.deleteTexture(_iconTextureId);
            _iconTextureId = BitmapUtil.createTextureFromDrawable(_icon, (int)_context.heightOf(this), (int)_context.heightOf(this));
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