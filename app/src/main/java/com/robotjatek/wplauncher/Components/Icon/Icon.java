package com.robotjatek.wplauncher.Components.Icon;

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

    private static final int ICON_TEX_SIZE_PX = 512;
    private boolean _disposed = false;
    private final float[] _modelMatrix = new float[16];
    private Drawable _iconDrawable;
    private int _textureId = -1;
    private int _bgColorTextureId = -1;
    private int _bgColor;
    private boolean _dirty = true;
    private Size<Integer> _size = new Size<>(0, 0);

    public Icon(Drawable iconDrawable, int bgColor) {
        _iconDrawable = iconDrawable;
        _bgColor = bgColor;
    }

    public Icon(Drawable iconDrawable) {
        this(iconDrawable, Colors.TRANSPARENT);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int)drawContext.widthOf(this);
        var h = (int)drawContext.heightOf(this);

        if (_dirty) {
            if (_textureId > 0) {
                TileUtil.deleteTexture(_textureId);
            }
            if (_bgColorTextureId > 0) {
                TileUtil.deleteTexture(_bgColorTextureId);
            }
            _textureId = BitmapUtil.createTextureFromDrawable(_iconDrawable, ICON_TEX_SIZE_PX, ICON_TEX_SIZE_PX);
            if (_bgColor != Colors.TRANSPARENT) {
                _bgColorTextureId = BitmapUtil.createTextureFromBitmap(BitmapUtil.createRect(1, 1, 0, _bgColor));
            }
            _dirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);

        if (_bgColorTextureId > 0) {
            renderer.draw(proj, _modelMatrix, _bgColorTextureId);
        }

        renderer.draw(proj, _modelMatrix, _textureId);
    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    public void setSize(Size<Integer> size) {
        _size = size;
        _dirty = true;
    }

    public void setIconDrawable(Drawable icon) {
        _iconDrawable = icon;
        _dirty = true;
    }

    public void setBgColor(int bgColor) {
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
            if (_bgColorTextureId > 0) {
                TileUtil.deleteTexture(_bgColorTextureId);
                _bgColorTextureId = -1;
            }
            _disposed = true;
        }
    }

    @Override
    public void onTap() {
        // Ignore for now
    }
}
