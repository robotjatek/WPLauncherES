package com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Layouts.LayoutInfo;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileUtil;

import java.util.ArrayList;
import java.util.List;


public class AbsoluteLayout implements ILayout {
    private boolean _disposed = false;
    private final List<PositionedElement> _positionedElements = new ArrayList<>();
    private Size<Integer> _size = new Size<>(-1, -1);
    private final float[] _modelMatrix = new float[16];
    private int _bgColor = Colors.TRANSPARENT;
    private int _bgTexture = -1;
    private boolean _dirty = true;
    private final IDrawContext<UIElement> _drawContext = new AbsoluteLayoutDrawContext(this);

    public static class PositionedElement {
        UIElement _element;
        Position<Float> _position;

        PositionedElement(UIElement element, Position<Float> position) {
            _element = element;
            _position = position;
        }
    }

    public void addChild(UIElement element, Position<Float> position) {
        _positionedElements.add(new PositionedElement(element, position));
        _dirty = true;
    }

    public void removeChild(UIElement element) {
        _positionedElements.removeIf(layout -> layout._element.equals(element));
    }

    public void setChildPosition(UIElement element, Position<Float> position) {
        for (var child : _positionedElements) {
            if (child._element == element) {
                child._position = position;
                _dirty = true;
                return;
            }
        }
    }

    public void setBgColor(int bgColor) {
        _bgColor = bgColor;
        _dirty = true;
    }

    public List<PositionedElement> getPositionedElements() {
        return _positionedElements;
    }

    @Override
    public IDrawContext<UIElement> getContext() {
        return _drawContext;
    }

    @Override
    public void onResize(int width, int height) {
        _size = new Size<>(width, height);
        _dirty = true;
    }

    @Override
    public int getWidth() {
        return _size.width();
    }

    @Override
    public int getHeight() {
        return _size.height();
    }

    @Override
    public LayoutInfo getLayoutInfo(UIElement item) {
        return null;
    }

    @Override
    public void draw(float delta, float[] proj, float[] viewMatrix, QuadRenderer renderer,
                     Position<Float> position, Size<Integer> size) {
        if (!_size.equals(size)) {
            _size = size;
            _dirty = true;
        }

        if (_dirty) {
            TileUtil.deleteTexture(_bgTexture);
            _bgTexture = BitmapUtil.createTextureFromBitmap(BitmapUtil.createRect(1, 1, 0, _bgColor));
            _dirty = false;
        }

        // Draw background
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, position.x(), position.y(), 0f);
        Matrix.scaleM(_modelMatrix, 0, size.width(), size.height(), 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _bgTexture);

        // Draw children with offset
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, position.x(), position.y(), 0f);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);

        for (var child : _positionedElements) {
            child._element.draw(proj, _modelMatrix, _drawContext, renderer);
        }
    }

    @Override
    public void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var width = (int) drawContext.widthOf(this);
        var height = (int) drawContext.heightOf(this);

        // TODO: add delta to the interface
        draw(0, proj, view, renderer, new Position<>(x, y), new Size<>(width, height));
    }

    @Override
    public Size<Integer> measure() {
        if (_size.width() != -1 && _size.height() != -1) {
            return _size;
        }
        // Calculation based on children positions and sizes
        float maxX = 0;
        float maxY = 0;
        for (var pe : _positionedElements) {
            var size = pe._element.measure();
            maxX = Math.max(maxX, pe._position.x() + size.width());
            maxY = Math.max(maxY, pe._position.y() + size.height());
        }
        return new Size<>((int) maxX, (int) maxY);
    }

    @Override
    public UIElement findChildAt(float x, float y) {
        // Iterate backwards to find topmost child (if they overlap)
        for (int i = _positionedElements.size() - 1; i >= 0; i--) {
            var pe = _positionedElements.get(i);
            var size = pe._element.measure();
            if (x >= pe._position.x() && x <= pe._position.x() + size.width() &&
                    y >= pe._position.y() && y <= pe._position.y() + size.height()) {
                return pe._element;
            }
        }
        return null;
    }

    public void clear() {
        _positionedElements.forEach(c -> c._element.dispose());
        _positionedElements.clear();
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _positionedElements.forEach(c -> c._element.dispose());
            _positionedElements.clear();
            TileUtil.deleteTexture(_bgTexture);
            _disposed = true;
        }
    }
}