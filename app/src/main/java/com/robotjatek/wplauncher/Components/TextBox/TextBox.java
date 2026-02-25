package com.robotjatek.wplauncher.Components.TextBox;

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

public class TextBox implements UIElement {
    private final float[] _modelMatrix = new float[16];
    private String _text;
    private int _textSize;
    private int _typeFace;
    private int _textColor;
    private int _bgColor;
    private int _maxWidth; // Maximum width before wrapping
    private int _maxHeight; // Maximum height (optional, -1 for unlimited)
    private float _lineSpacing = 1.2f; // Line height multiplier
    private boolean _dirty = true;
    private int _textureId = -1;
    private Size<Integer> _cachedSize = new Size<>(0, 0);

    public TextBox(String text, int textSize, int typeFace, int textColor, int bgColor, int maxWidth) {
        this(text, textSize, typeFace, textColor, bgColor, maxWidth, -1);
    }

    public TextBox(String text, int textSize, int typeFace, int textColor, int bgColor, int maxWidth, int maxHeight) {
        _text = text;
        _textSize = textSize;
        _typeFace = typeFace;
        _textColor = textColor;
        _bgColor = bgColor;
        _maxWidth = maxWidth;
        _maxHeight = maxHeight;
    }

    @Override
    public void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);

        if (_dirty) {
            if (_textureId > 0) {
                TileUtil.deleteTexture(_textureId);
            }

            var size = measure();
            if (size.width() == 0 || size.height() == 0) {
                return; // Do not draw invisible element
            }

            _textureId = createMultilineTexture();
            _dirty = false;
        }

        if (_cachedSize.width() == 0 || _cachedSize.height() == 0) {
            return; // Do not draw invisible element
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, _cachedSize.width(), _cachedSize.height(), 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _textureId);
    }

    private int createMultilineTexture() {
        var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
        paint.setTextSize(_textSize);
        paint.setColor(_textColor);

        // Wrap text into lines
        var lines = wrapText(_text, _maxWidth, paint);

        // Calculate total height
        var fm = paint.getFontMetrics();
        var lineHeight = (int) ((fm.descent - fm.ascent) * _lineSpacing);
        var totalHeight = lineHeight * lines.size();

        // Apply max height constraint if set
        if (_maxHeight > 0 && totalHeight > _maxHeight) {
            totalHeight = _maxHeight;
            // Trim lines that don't fit
            int maxLines = _maxHeight / lineHeight;
            if (lines.size() > maxLines) {
                lines = lines.subList(0, maxLines);
                // Add ellipsis to last line if text was cut off
                if (!lines.isEmpty()) {
                    var lastLine = lines.get(lines.size() - 1);
                    lines.set(lines.size() - 1, lastLine + "...");
                }
            }
        }

        _cachedSize = new Size<>(_maxWidth, totalHeight);

        // Create bitmap and draw text
        var bitmap = Bitmap.createBitmap(_maxWidth, totalHeight, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);

        // Draw background
        canvas.drawColor(_bgColor);

        // Draw each line
        var baselineY = -fm.ascent; // First baseline
        for (var line : lines) {
            canvas.drawText(line, 0, baselineY, paint);
            baselineY += lineHeight;
        }

        var textureId = BitmapUtil.createTextureFromBitmap(bitmap);
        bitmap.recycle();
        return textureId;
    }

    private List<String> wrapText(String text, int maxWidth, Paint paint) {
        var lines = new ArrayList<String>();

        // Split by explicit line breaks first
        var paragraphs = text.split("\n");

        for (var paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            var words = paragraph.split(" ");
            var currentLine = new StringBuilder();

            for (var word : words) {
                var testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                var testWidth = paint.measureText(testLine);

                if (testWidth > maxWidth && currentLine.length() > 0) {
                    // Current line is full, start a new one
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }

            // Add the last line of the paragraph
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    @Override
    public Size<Integer> measure() {
        if (_dirty) {
            var paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.create("sans-serif-light", _typeFace));
            paint.setTextSize(_textSize);

            var lines = wrapText(_text, _maxWidth, paint);
            var fm = paint.getFontMetrics();
            var lineHeight = (int) ((fm.descent - fm.ascent) * _lineSpacing);
            var totalHeight = lineHeight * lines.size();

            if (_maxHeight > 0 && totalHeight > _maxHeight) {
                totalHeight = _maxHeight;
            }

            _cachedSize = new Size<>(_maxWidth, totalHeight);
        }

        return _cachedSize;
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

    public void setMaxHeight(int maxHeight) {
        _maxHeight = maxHeight;
        _dirty = true;
    }

    public void setLineSpacing(float lineSpacing) {
        _lineSpacing = lineSpacing;
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