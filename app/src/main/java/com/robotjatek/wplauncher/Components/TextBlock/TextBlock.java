package com.robotjatek.wplauncher.Components.TextBlock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileUtil;

import java.util.ArrayList;
import java.util.List;

public class TextBlock implements UIElement {
    private boolean _disposed = false;
    private final float[] _modelMatrix = new float[16];
    private String _text;
    private int _textSize;
    private int _typeFace;
    private int _textColor;
    private int _bgColor;
    private int _maxWidth;
    private int _maxHeight; // Maximum height (optional, -1 for unlimited)
    private float _lineSpacing = 1.2f;
    private boolean _dirty = true;
    private int _textureId = -1;
    private Size<Integer> _cachedSize = new Size<>(0, 0);
    private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<String> _wrappedLines = null; // Cache for wrapped lines

    public TextBlock(String text, int textSize, int typeFace, int textColor, int bgColor, int maxWidth) {
        this(text, textSize, typeFace, textColor, bgColor, maxWidth, -1);
    }

    public TextBlock(String text, int textSize, int typeFace, int textColor, int bgColor, int maxWidth, int maxHeight) {
        _text = text;
        _textSize = textSize;
        _typeFace = typeFace;
        _textColor = textColor;
        _bgColor = bgColor;
        _maxWidth = maxWidth;
        _maxHeight = maxHeight;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);

        if (_dirty || _textureId == -1) {
            measure();
            if (_cachedSize.width() <= 0 || _cachedSize.height() <= 0 || _wrappedLines == null) {
                return;
            }
            if (_textureId != -1) {
                TileUtil.deleteTexture(_textureId);
            }
            _textureId = createMultilineTexture();
            _dirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, _cachedSize.width(), _cachedSize.height(), 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _textureId);
    }

    private int createMultilineTexture() {
        _paint.setTextAlign(Paint.Align.LEFT);
        _paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
        _paint.setTextSize(_textSize);
        _paint.setColor(_textColor);

        var lines = _wrappedLines;
        var fm = _paint.getFontMetrics();
        var lineHeight = (int) ((fm.descent - fm.ascent) * _lineSpacing);
        var totalHeight = _cachedSize.height();

        var bitmap = Bitmap.createBitmap(_cachedSize.width(), totalHeight, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        canvas.drawColor(_bgColor);

        var baselineY = -fm.ascent;
        for (var line : lines) {
            if (baselineY > totalHeight) break;
            canvas.drawText(line, 0, baselineY, _paint);
            baselineY += lineHeight;
        }

        var textureId = BitmapUtil.createTextureFromBitmap(bitmap);
        bitmap.recycle();
        return textureId;
    }

    private List<String> wrapText(String text, int maxWidth, Paint paint) {
        if (maxWidth <= 0) return List.of(text.split("\n", -1));

        var lines = new ArrayList<String>();
        for (var paragraph : text.split("\n", -1)) {
            int start = 0;
            while (start < paragraph.length()) {
                var count = paint.breakText(paragraph, start, paragraph.length(), true, maxWidth, null);
                if (count <= 0) break;
                var end = start + count;
                if (end < paragraph.length()) {
                    var lastSpace = paragraph.lastIndexOf(' ', end);
                    if (lastSpace > start) end = lastSpace;
                }
                lines.add(paragraph.substring(start, end));
                start = (end == start + count) ? end : end + 1;
            }
        }
        return lines;
    }

    @Override
    public Size<Integer> measure() {
        if (_dirty || _wrappedLines == null) {
            _paint.setTextAlign(Paint.Align.LEFT);
            _paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
            _paint.setTextSize(_textSize);

            _wrappedLines = wrapText(_text, _maxWidth, _paint);
            var fm = _paint.getFontMetrics();
            var lineHeight = (int) ((fm.descent - fm.ascent) * _lineSpacing);

            var totalHeight = lineHeight * _wrappedLines.size();

            if (_maxHeight > 0 && totalHeight > _maxHeight) {
                totalHeight = _maxHeight;
                var maxLines = _maxHeight / lineHeight;
                if (_wrappedLines.size() > maxLines) {
                    _wrappedLines = new ArrayList<>(_wrappedLines.subList(0, maxLines));
                    if (!_wrappedLines.isEmpty()) {
                        var last = _wrappedLines.size() - 1;
                        _wrappedLines.set(last, _wrappedLines.get(last) + "...");
                    }
                }
            }

            _cachedSize = new Size<>(Math.max(0, _maxWidth), totalHeight);
            _dirty = false;
        }
        return _cachedSize;
    }

    public String getText() {
        return _text;
    }

    private void invalidate() {
        _dirty = true;
        _wrappedLines = null;
        if (_textureId != -1) {
            TileUtil.deleteTexture(_textureId);
            _textureId = -1;
        }
    }

    public void setText(String text) {
        _text = text;
        invalidate();
    }

    public void setMaxWidth(int maxWidth) {
        if (_maxWidth != maxWidth) {
            _maxWidth = maxWidth;
            invalidate();
        }
    }

    public void setMaxHeight(int maxHeight) {
        if (_maxHeight != maxHeight) {
            _maxHeight = maxHeight;
            invalidate();
        }
    }

    public void setLineSpacing(float lineSpacing) {
        if (_lineSpacing != lineSpacing) {
            _lineSpacing = lineSpacing;
            invalidate();
        }
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
    public void dispose() {
        if (!_disposed) {
            TileUtil.deleteTexture(_textureId);
            _textureId = -1;
            _disposed = true;
        }
    }
}