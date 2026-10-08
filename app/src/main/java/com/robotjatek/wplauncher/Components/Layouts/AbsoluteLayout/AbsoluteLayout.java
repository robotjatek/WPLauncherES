package com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Layouts.LayoutInfo;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Theme.ITheme;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;


public class AbsoluteLayout implements ILayout {
    private boolean _disposed = false;
    private final List<PositionedElement> _positionedElements = new CopyOnWriteArrayList<>();
    private Size<Integer> _size = new Size<>(-1, -1);
    private final float[] _modelMatrix = new float[16];
    private Integer _bgColor = null;
    private final IDrawContext<UIElement> _drawContext = new AbsoluteLayoutDrawContext(this);
    private ILayout _parent;
    private final Supplier<ITheme.LayoutStyle> _styleSupplier;

    public AbsoluteLayout(Supplier<ITheme.LayoutStyle> style) {
        _styleSupplier = style;
    }

    private static final ITheme.LayoutStyle LEGACY = new ITheme.LayoutStyle(Colors.TRANSPARENT);

    // TODO: remove legacy constructor
    public AbsoluteLayout() {
        this(() -> LEGACY);
    }

    public static class PositionedElement {
        UIElement _element;
        Position<Float> _position;

        PositionedElement(UIElement element, Position<Float> position) {
            _element = element;
            _position = position;
        }
    }

    @Override
    public void setParent(ILayout parent) {
        _parent = parent;
    }

    public void addChild(UIElement element, Position<Float> position) {
        _positionedElements.add(new PositionedElement(element, position));
        element.setParent(this);
    }

    public void removeChild(UIElement element) {
        _positionedElements.removeIf(layout -> layout._element.equals(element));
        element.setParent(null);
    }

    public void removeAll() {
        _positionedElements.forEach(e -> e._element.setParent(null));
        _positionedElements.clear();
    }

    public void setChildPosition(UIElement element, Position<Float> position) {
        if (!isPresent(element)) {
            addChild(element, position);
        }

        for (var child : _positionedElements) {
            if (child._element == element) {
                child._position = position;
                return;
            }
        }
    }

    private boolean isPresent(UIElement element) {
        return _positionedElements.stream().anyMatch(x -> x._element == element);
    }

    public boolean hasContent() {
        return !_positionedElements.isEmpty();
    }

    public void setBgColor(Integer bgColor) {
        _bgColor = bgColor;
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
        }

        renderer.pushLayer();

        // Draw background
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, position.x(), position.y(), 0f);
        Matrix.scaleM(_modelMatrix, 0, size.width(), size.height(), 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        var bgColor = _bgColor == null ? _styleSupplier.get().bgColor() : _bgColor;
        renderer.drawFlat(proj, _modelMatrix, bgColor);

        // Draw children with offset
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, position.x(), position.y(), 0f);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);

        for (var child : _positionedElements) {
            renderer.pushLayer();
            child._element.draw(delta, proj, _modelMatrix, _drawContext, renderer);
            renderer.popLayer();
        }
        renderer.popLayer();
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var width = (int) drawContext.widthOf(this);
        var height = (int) drawContext.heightOf(this);

        draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(width, height));
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
        var size = new Size<>((int) maxX, (int) maxY);
        if (!_size.equals(size)) {
            _size = size;
            if (_parent != null) {
                _parent.layout();
            }
        }

        return _size;
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

    @Override
    public void layout() {
        if (_parent != null) {
            _parent.layout();
        }
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _positionedElements.forEach(c -> c._element.dispose());
            _positionedElements.clear();
            _disposed = true;
        }
    }
}