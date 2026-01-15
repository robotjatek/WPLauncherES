package com.robotjatek.wplauncher.Components.Label;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.HorizontalAlign;
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
    public void draw(float[] proj, float[] view, ILayout layout) {
        var context = layout.getContext();
        var x = context.xOf(this);
        var y = context.yOf(this);
        var w = context.widthOf(this);
        var h = context.heightOf(this);

        if (_dirty) {
            if (_textureId > 0) {
                TileUtil.deleteTexture(_textureId);
            }
            _textureId = TileUtil.createTextTexture(_text,
                    (int) w,
                    (int) h,
                    _textSize,
                    _typeFace,
                    _textColor,
                    _bgColor,
                    HorizontalAlign.LEFT,
                    VerticalAlign.CENTER);
            _dirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        layout.getContext().getRenderer().draw(proj, _modelMatrix, _textureId);
    }

    @Override
    public Size measure() {
        var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
        paint.setTextSize(_textSize);
        var textWidth = paint.measureText(_text);
        var fm = paint.getFontMetrics();
        var textHeight = fm.descent - fm.ascent;
        return new Size(textWidth, textHeight);
    }

    public String getText() {
        return _text;
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
