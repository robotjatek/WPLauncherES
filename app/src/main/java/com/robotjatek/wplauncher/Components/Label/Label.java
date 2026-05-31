package com.robotjatek.wplauncher.Components.Label;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

public class Label implements UIElement, ITouchable {
    private boolean _disposed = false;
    private final float[] _modelMatrix = new float[16];
    private String _text;
    private int _textSize;
    private int _typeFace;
    private int _textColor;
    private int _bgColor;
    private float _scale = 1f;
    private float _maxWidth; // -1 means no limit
    private boolean _dirty = true;
    private int _textureId = -1;
    private final Runnable _onTap;
    private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TouchHandler _touchHandler = new TouchHandler(this);

    public Label(String text, int textSize, int typeFace, int textColor, int bgColor) {
        this(text, textSize, typeFace, textColor, bgColor, -1, null);
    }

    public Label(String text, int textSize, int typeFace, int textColor, int bgColor, int maxWidth, Runnable onTap) {
        _text = text;
        _textSize = textSize;
        _typeFace = typeFace;
        _textColor = textColor;
        _bgColor = bgColor;
        _maxWidth = maxWidth;
        _onTap = onTap;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _touchHandler.update(delta);
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this) * _scale;
        var h = (int) drawContext.heightOf(this) * _scale;

        var xDiff = (w - drawContext.widthOf(this)) / 2f;
        var yDiff = (h - drawContext.heightOf(this)) / 2f;
        var correctedX = x - xDiff;
        var correctedY = y - yDiff;

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
                    (int) w,
                    (int) h,
                    (int) (_textSize * _scale),
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
        Matrix.translateM(_modelMatrix, 0, correctedX, correctedY, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _textureId);
    }

    private String truncateText(String text, float maxWidth) {
        _paint.setTextAlign(Paint.Align.LEFT);
        _paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
        _paint.setTextSize(_textSize);

        var textWidth = _paint.measureText(text);

        // Text fits - no truncation needed
        if (textWidth <= maxWidth) {
            return text;
        }

        // Need to truncate - binary search for the right length
        var ellipsis = "...";
        var ellipsisWidth = _paint.measureText(ellipsis);
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
            var substringWidth = _paint.measureText(substring);

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
        _paint.setTextAlign(Paint.Align.LEFT);
        _paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
        _paint.setTextSize(_textSize);

        var displayText = _maxWidth > 0 ? truncateText(_text, _maxWidth) : _text;
        var textWidth = _paint.measureText(displayText);

        // If max width is set, don't exceed it
        if (_maxWidth > 0 && textWidth > _maxWidth) {
            textWidth = _maxWidth;
        }

        var fm = _paint.getFontMetrics();
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

    public void setMaxWidth(float maxWidth) {
        _maxWidth = maxWidth;
        _dirty = true;
    }

    public int getBgColor() {
        return _bgColor;
    }

    public int getTextColor() {
        return _textColor;
    }

    public void setTextColor(int color) {
        _textColor = color;
        _dirty = true;
    }

    public int getTextSize() {
        return _textSize;
    }

    public void setTextSize(int size) {
        _textSize = size;
        _dirty = true;
    }

    public Typeface getTypeFace() {
        return _paint.getTypeface();
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        if (_onTap == null) {
            return false;
        }

        _touchHandler.onDown(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        if (_onTap == null) {
            return false;
        }

        _touchHandler.onUp();
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _touchHandler.onMove(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public void onPress() {
        _scale = 0.95f;
    }

    @Override
    public void onRelease() {
        _scale = 1f;
    }

    @Override
    public void onAction() {
        if (_onTap != null) {
            _onTap.run();
        }
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            TileUtil.deleteTexture(_textureId);
            _disposed = true;
        }
    }
}