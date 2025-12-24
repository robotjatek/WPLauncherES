package com.robotjatek.wplauncher.TileGrid;

import android.graphics.drawable.Drawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.TileUtil;

public class Adorner {
    private final IAdornerRenderingContext _context;
    private final Runnable _onTap;
    private final int _textureId;
    private final float[] _modelMatrix = new float[16];

    public Adorner(Runnable onTap, Drawable icon, IAdornerRenderingContext context) {
        _context = context;
        _onTap = onTap;
        _textureId = BitmapUtil.createTextureFromDrawable(icon, 96, 96);
    }

    public void draw(float[] proj, float[] view) {
        var x = _context.xOf(this);
        var y = _context.yOf(this);
        var w = _context.widthOf(this);
        var h = _context.heightOf(this);

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 1);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        _context.getRenderer().draw(proj, _modelMatrix, _textureId);
    }

    public void onTap() {
        _onTap.run();
    }

    public boolean isTapped(float x, float y) {
        var left = _context.xOf(this);
        var top = _context.yOf(this);
        var right = left + _context.widthOf(this);
        var bottom = top + _context.heightOf(this);

        return x >= left && x <= right && y >= top && y <= bottom;
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
    }
}
