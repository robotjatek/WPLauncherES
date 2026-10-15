package com.robotjatek.wplauncher.Components.TextBlock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Theme.ITheme;
import com.robotjatek.wplauncher.TileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TextBlock implements UIElement {
    private boolean _disposed = false;
    private final float[] _modelMatrix = new float[16];
    private String _text;
    private int _maxWidth;
    private int _maxHeight; // Maximum height (optional, -1 for unlimited)
    private final float _lineSpacing = 1.2f;
    private boolean _dirty = true;
    record TextureChunk(int textureId, int width, int height, int xOffset, int yOffset) {}
    private final List<TextureChunk> _textureChunks = new ArrayList<>();
    private Size<Integer> _cachedSize = new Size<>(0, 0);
    private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<String> _wrappedLines = null; // Cache for wrapped lines
    private ILayout _parent;
    private final Supplier<ITheme.TextBlockStyle> _styleSupplier;
    private ITheme.TextBlockStyle _currentStyle;

    public TextBlock(String text, Supplier<ITheme.TextBlockStyle> style, int maxWidth) {
        this(text, style, maxWidth, -1);
    }

    public TextBlock(String text, Supplier<ITheme.TextBlockStyle> style, int maxWidth, int maxHeight) {
        _text = text;
        _styleSupplier = style;
        _currentStyle = style.get();
        _maxWidth = maxWidth;
        _maxHeight = maxHeight;
    }

    private void syncStyle() {
        var style = _styleSupplier.get();
        if (!style.equals(_currentStyle)) {
            _currentStyle = style;
            invalidate();
        }
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);

        syncStyle();
        if (_dirty || _textureChunks.isEmpty()) {
            measure();
            if (_cachedSize.width() <= 0 || _cachedSize.height() <= 0 || _wrappedLines == null) {
                return;
            }
            createChunks(renderer);
            _dirty = false;
        }

        // TODO: only render visible chunks
        for (var chunk : _textureChunks) {
            Matrix.setIdentityM(_modelMatrix, 0);
            Matrix.translateM(_modelMatrix, 0, x + chunk.xOffset, y + chunk.yOffset, 0);
            Matrix.scaleM(_modelMatrix, 0, chunk.width, chunk.height, 1f);
            Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
            renderer.draw(proj, _modelMatrix, chunk.textureId);
        }
    }

    // TODO: only create chunks that are actually visible
    private void createChunks(QuadRenderer renderer) {
        clearChunks();

        _paint.setTextAlign(Paint.Align.LEFT);
        _paint.setTypeface(Typeface.create("sans-serif-light", _currentStyle.typeface()));
        _paint.setTextSize(_currentStyle.textSize());
        _paint.setColor(_currentStyle.textColor());

        var totalHeight = _cachedSize.height();
        var totalWidth = _cachedSize.width();
        var maxTextureSize = renderer.getMaxTextureSize();
        var numberOfXChunks = (int)Math.ceil((float)totalWidth / maxTextureSize);
        var numberOfYChunks = (int)Math.ceil((float)totalHeight / maxTextureSize);

        var fm = _paint.getFontMetrics();
        var lineHeight = (int)((fm.descent - fm.ascent) * _lineSpacing);
        var firstLineBaseline = -fm.ascent;

        for (var yId = 0; yId < numberOfYChunks; yId++) {
            var startY = yId * maxTextureSize;
            var chunkHeight = Math.min(maxTextureSize, totalHeight - startY);
            var endY = startY + chunkHeight;

            var startLineIdx = Math.max(0, (int) Math.floor((startY - firstLineBaseline) / (double) lineHeight));
            var endLineIdx = Math.min(_wrappedLines.size(), (int) Math.ceil((endY - firstLineBaseline) / (double) lineHeight));

            for (var xId = 0; xId < numberOfXChunks; xId++) {
                var startX = xId * maxTextureSize;
                var chunkWidth = Math.min(maxTextureSize, totalWidth - startX);

                var chunkBitmap = Bitmap.createBitmap(chunkWidth, chunkHeight, Bitmap.Config.ARGB_8888);
                var canvas = new Canvas(chunkBitmap);
                canvas.drawColor(_currentStyle.bgColor());

                var baseLineY = firstLineBaseline + (startLineIdx * lineHeight);
                for (var i = startLineIdx; i < endLineIdx; i++) {
                    canvas.drawText(_wrappedLines.get(i), -startX, baseLineY - startY, _paint);
                    baseLineY += lineHeight;
                }

                var textureId = BitmapUtil.createTextureFromBitmap(chunkBitmap);
                _textureChunks.add(new TextureChunk(textureId, chunkWidth, chunkHeight, startX, startY));
                chunkBitmap.recycle();
            }
        }
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
                lines.add(paragraph.substring(start, end).trim());
                start = (end == start + count) ? end : end + 1;
            }
        }
        return lines;
    }

    @Override
    public Size<Integer> measure() {
        if (_dirty || _wrappedLines == null) {
            _paint.setTextAlign(Paint.Align.LEFT);
            _paint.setTypeface(Typeface.create("sans-serif-light", _currentStyle.typeface()));
            _paint.setTextSize(_currentStyle.textSize());

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

            var size = new Size<>(Math.max(0, _maxWidth), totalHeight);
            if (!_cachedSize.equals(size)) {
                _cachedSize = size;
                if (_parent != null) {
                    _parent.layout();
                }
            }
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
        clearChunks();
    }

    public void setText(String text) {
        if (_text.equals(text)) return;

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

    @Override
    public void setParent(ILayout parent) {
        _parent = parent;
    }

    private void clearChunks() {
        for (var chunk : _textureChunks) {
            TileUtil.deleteTexture(chunk.textureId);
        }
        _textureChunks.clear();
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            clearChunks();
            _disposed = true;
        }
    }
}