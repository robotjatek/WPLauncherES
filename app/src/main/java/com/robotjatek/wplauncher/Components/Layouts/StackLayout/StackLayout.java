package com.robotjatek.wplauncher.Components.Layouts.StackLayout;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Layouts.LayoutInfo;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class StackLayout implements ILayout {
    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    private boolean _disposed = false;
    private int _bgColor = Colors.TRANSPARENT;
    public static final int TOP_MARGIN_PX = 0;
    private final List<UIElement> _children = new CopyOnWriteArrayList<>();
    private final Map<UIElement, LayoutInfo> _layoutInfo = new ConcurrentHashMap<>();
    private final StackLayoutDrawContext _drawContext;
    private final Orientation _orientation;
    private int _width;
    private int _height;
    private final float[] _model = new float[16];

    public StackLayout() {
        this(Orientation.VERTICAL);
    }

    public StackLayout(Orientation orientation) {
        _orientation = orientation;
        _drawContext = new StackLayoutDrawContext(this);
    }

    @Override
    public LayoutInfo getLayoutInfo(UIElement item) {
        return _layoutInfo.get(item);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, QuadRenderer renderer, Position<Float> position,
                     Size<Integer> size) {

        Matrix.setIdentityM(_model, 0);
        Matrix.translateM(_model, 0, position.x(), position.y(), 0f);
        Matrix.scaleM(_model, 0, size.width(), size.height(), 1f);
        Matrix.multiplyMM(_model, 0, view, 0, _model, 0);
        renderer.drawFlat(proj, _model, _bgColor);

        renderer.pushLayer();
        Matrix.setIdentityM(_model, 0);
        Matrix.translateM(_model, 0, position.x(), position.y() + TOP_MARGIN_PX, 0f);
        Matrix.multiplyMM(_model, 0, view, 0, _model, 0);
        for (var child : _children) {
            child.draw(delta, proj, _model, _drawContext, renderer);
        }
        renderer.popLayer();
    }

    public void addChild(UIElement element) {
        _children.add(element);
        layout();
    }

    @Override
    public IDrawContext<UIElement> getContext() {
        return _drawContext;
    }

    @Override
    public void onResize(int width, int height) {
        _width = width;
        _height = height;
        layout();
    }

    @Override
    public int getWidth() {
        return _width;
    }

    @Override
    public int getHeight() {
        return _height;
    }

    public void layout() {
        _layoutInfo.clear();
        var offset = 0f;
        for (var child : _children) {
            var size = child.measure();
            if (_orientation == Orientation.VERTICAL) {
                _layoutInfo.put(child, new LayoutInfo(0, offset));
                offset += size.height();
            } else {
                _layoutInfo.put(child, new LayoutInfo(offset, 0));
                offset += size.width();
            }
        }
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
        var totalHeight = 0;
        var totalWidth = 0;
        var maxChildWidth = 0;
        var maxChildHeight = 0;
        for (var child : _children) {
            var size = child.measure();
            if (_orientation == Orientation.VERTICAL) {
                totalHeight += size.height();
                maxChildWidth = Math.max(maxChildWidth, size.width());
            } else {
                totalWidth += size.width();
                maxChildHeight = Math.max(maxChildHeight, size.height());
            }

        }
        return _orientation == Orientation.VERTICAL ?
                new Size<>(maxChildWidth, totalHeight) :
                new Size<>(totalWidth, maxChildHeight);
    }

    public Orientation getOrientation() {
        return _orientation;
    }

    @Override
    public UIElement findChildAt(float x, float y) {
        for (var child : _children) {
            var left = _drawContext.xOf(child);
            var top = _drawContext.yOf(child) + TOP_MARGIN_PX;
            var right = left + _drawContext.widthOf(child);
            var bottom = top + _drawContext.heightOf(child);
            if (x >= left && x <= right && y >= top && y <= bottom) {
                return child;
            }
        }
        return null;
    }

    public void setBgColor(int color) {
        _bgColor = color;
    }

    public void dispose() {
        if (!_disposed) {
            _children.forEach(UIElement::dispose);
            _children.clear();
            _layoutInfo.clear();
            _disposed = true;
        }
    }
}
