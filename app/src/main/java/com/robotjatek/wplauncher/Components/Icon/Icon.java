package com.robotjatek.wplauncher.Components.Icon;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileUtil;

public class Icon implements UIElement {
    private boolean _disposed = false;
    private final float[] _modelMatrix = new float[16];
    private Drawable _iconDrawable;
    private Bitmap _pendingBitmap;
    private int _textureId = -1;
    private int _bgColor;
    private boolean _dirty = true;
    private Size<Integer> _size;

    public Icon(Drawable iconDrawable, int bgColor, Size<Integer> size) {
        _iconDrawable = iconDrawable;
        _bgColor = bgColor;
        _size = size;
    }

    public Icon(Drawable iconDrawable, Size<Integer> size) {
         this(iconDrawable, Colors.TRANSPARENT, size);
    }

    public Icon(int _bgColor, Size<Integer> size) {
        this(null, _bgColor, size);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int)drawContext.widthOf(this);
        var h = (int)drawContext.heightOf(this);

        if (_dirty) {
            // Only recreate texture if we have a NEW source waiting
            if (_pendingBitmap != null || _iconDrawable != null) {
                if (_textureId > 0) {
                    TileUtil.deleteTexture(_textureId);
                    _textureId = -1;
                }

                if (_pendingBitmap != null) {
                    _textureId = BitmapUtil.createTextureFromBitmap(_pendingBitmap);
                    _pendingBitmap = null;
                } else if (_iconDrawable != null) {
                    _textureId = BitmapUtil.createTextureFromDrawable(_iconDrawable, _size.width(), _size.height());
                }
            }
            _dirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 1f);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);

        if (_bgColor != Colors.TRANSPARENT) {
            renderer.drawFlat(proj, _modelMatrix, _bgColor);
        }

        if (_textureId > 0) {
            renderer.draw(proj, _modelMatrix, _textureId);
        }
    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    public void setSize(Size<Integer> size) {
        if (_size.equals(size)) return;
        _size = size;
        _dirty = true;
    }

    public void setIconDrawable(Drawable icon) {
        _iconDrawable = icon;
        _pendingBitmap = null;
        _dirty = true;
    }

    public void setBitmap(Bitmap bitmap) {
        _pendingBitmap = bitmap;
        _iconDrawable = null;
        _dirty = true;
    }

    public void setBgColor(int bgColor) {
        if (_bgColor == bgColor) return;

        _bgColor = bgColor;
        _dirty = true;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            if (_textureId > 0) {
                TileUtil.deleteTexture(_textureId);
                _textureId = -1;
            }
            if (_pendingBitmap != null) {
                _pendingBitmap.recycle();
                _pendingBitmap = null;
            }
            _disposed = true;
        }
    }
}
