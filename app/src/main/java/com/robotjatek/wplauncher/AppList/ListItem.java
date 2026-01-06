package com.robotjatek.wplauncher.AppList;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.VerticalAlign;
import com.robotjatek.wplauncher.TileUtil;

public class ListItem<T> {
    private String _label;
    private final Drawable _icon;
    private int _iconTextureId;
    private int _textureId;
    private final float[] _modelMatrix = new float[16];
    private final Runnable _onTap;
    private boolean _dirty = true;
    private final T _payload;

    public ListItem(String label, Drawable icon, IDrawContext<ListItem<T>> context, Runnable onTap, T payload) {
        _label = label;
        _onTap = onTap;
        _payload = payload;
        _icon = icon;
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

    public void draw(float[] projMatrix, float[] viewMatrix, IDrawContext<ListItem<T>> _context) {
        var x = _context.xOf(this);
        var y = _context.yOf(this);
        var w = _context.widthOf(this);
        var h = _context.heightOf(this);

        var labelW = w - h;
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
        Matrix.scaleM(_modelMatrix, 0, labelW, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        _context.getRenderer().draw(projMatrix, _modelMatrix, _textureId);
    }

    public void update(IDrawContext<ListItem<T>> context) {
        // TODO: queue up opengl events into a command list
        if (_dirty) {
            var w = context.widthOf(this);
            var h = context.heightOf(this);
            var labelW = (int)(w - h);
            TileUtil.deleteTexture(_textureId);
            _textureId = TileUtil.createTextTexture(_label, labelW, (int)context.heightOf(this), 60,
                    Typeface.NORMAL, Colors.LIGHT_GRAY, 0, VerticalAlign.CENTER);
            TileUtil.deleteTexture(_iconTextureId);
            _iconTextureId = BitmapUtil.createTextureFromDrawable(_icon, (int)context.heightOf(this), (int)context.heightOf(this));
            _dirty = false;
        }
    }

    public void onTap() {
        if (_onTap != null) {
            _onTap.run();
        }
    }

    public void setDirty() {
        _dirty = true;
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        TileUtil.deleteTexture(_iconTextureId);
        _textureId = -1;
        _iconTextureId = -1;
    }
}