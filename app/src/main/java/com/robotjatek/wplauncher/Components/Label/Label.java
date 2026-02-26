package com.robotjatek.wplauncher.Components.Label;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;

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
    private int _maxWidth; // -1 means no limit
    private boolean _dirty = true;
    private int _textureId = -1;

    public Label(String text, int textSize, int typeFace, int textColor, int bgColor) {
        this(text, textSize, typeFace, textColor, bgColor, -1);
    }

    public Label(String text, int textSize, int typeFace, int textColor, int bgColor, int maxWidth) {
        _text = text;
        _textSize = textSize;
        _typeFace = typeFace;
        _textColor = textColor;
        _bgColor = bgColor;
        _maxWidth = maxWidth;
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

            // Truncate text if it exceeds max width
            var displayText = _maxWidth > 0 ? truncateText(_text, _maxWidth) : _text;

            _textureId = TileUtil.createTextTexture(displayText,
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

    private String truncateText(String text, int maxWidth) {
        var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
        paint.setTextSize(_textSize);

        var textWidth = paint.measureText(text);

        // Text fits - no truncation needed
        if (textWidth <= maxWidth) {
            return text;
        }

        // Need to truncate - binary search for the right length
        var ellipsis = "...";
        var ellipsisWidth = paint.measureText(ellipsis);
        var availableWidth = maxWidth - ellipsisWidth;

        if (availableWidth <= 0) {
            return ellipsis;
        }

        // Binary search for optimal length
        var left = 0;
        var right = text.length();
        var bestLength = 0;

        while (left <= right) {
            int mid = (left + right) / 2;
            var substring = text.substring(0, mid);
            var substringWidth = paint.measureText(substring);

            if (substringWidth <= availableWidth) {
                bestLength = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return text.substring(0, bestLength) + ellipsis;
    }

    @Override
    public Size<Integer> measure() {
        var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
        paint.setTextSize(_textSize);

        var displayText = _maxWidth > 0 ? truncateText(_text, _maxWidth) : _text;
        var textWidth = paint.measureText(displayText);

        // If max width is set, don't exceed it
        if (_maxWidth > 0 && textWidth > _maxWidth) {
            textWidth = _maxWidth;
        }

        var fm = paint.getFontMetrics();
        var textHeight = fm.descent - fm.ascent;
        return new Size<>((int)textWidth, (int)textHeight);
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
        _dirty = true;
    }

    public void setMaxWidth(int maxWidth) {
        _maxWidth = maxWidth;
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