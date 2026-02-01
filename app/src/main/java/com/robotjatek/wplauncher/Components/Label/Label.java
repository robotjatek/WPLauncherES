package com.robotjatek.wplauncher.Components.Label;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

public class Label implements UIElement {
    private final float[] _modelMatrix = new float[16];
    private String _text;
    private int _textSize;
    private int _typeFace;
    private int _textColor;
    private int _bgColor;
    private boolean _dirty = true;
    private int _textureId = -1;

    public Label(String text, int textSize, int typeFace, int textColor, int bgColor) {
        _text = text;
        _textSize = textSize;
        _typeFace = typeFace;
        _textColor = textColor;
        _bgColor = bgColor;
    }

    @Override
    public void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int)drawContext.widthOf(this);
        var h = (int)drawContext.heightOf(this);

        if (_dirty) {
            if (_textureId > 0) {
                TileUtil.deleteTexture(_textureId);
            }
            if (w == 0 || h == 0) {
                return; // Do not draw invisible element
            }
            _textureId = TileUtil.createTextTexture(_text,
                    w,
                    h,
                    _textSize,
                    _typeFace,
                    _textColor,
                    _bgColor,
                    HorizontalAlign.LEFT,
                    VerticalAlign.CENTER);
            _dirty = false;
        }

        if (w == 0 || h == 0) {
            return; // Do not draw invisible element
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _textureId);
    }

    @Override
    public Size<Float> measure() {
        var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
        paint.setTextSize(_textSize);
        var textWidth = paint.measureText(_text);
        var fm = paint.getFontMetrics();
        var textHeight = fm.descent - fm.ascent;
        return new Size<>(textWidth, textHeight);
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
        _dirty = true;
    }

    public int getBgColor() {
        return _bgColor;
    }

    public int getTextColor() {
        return _textColor;
    }

    public int getTextSize() {
        return _textSize;
    }

    public int getTypeFace() {
        return _typeFace;
    }

    @Override
    public void onTap() {
        // do nothing
    }

    @Override
    public void dispose() {
        TileUtil.deleteTexture(_textureId);
    }

}
