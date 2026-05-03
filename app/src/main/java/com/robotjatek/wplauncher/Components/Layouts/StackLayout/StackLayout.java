package com.robotjatek.wplauncher.Components.Layouts.StackLayout;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Layouts.LayoutInfo;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackLayout implements ILayout {
    private boolean _disposed = false;
    public static final int TOP_MARGIN_PX = 0;
    private final List<UIElement> _children = new ArrayList<>();
    private final Map<UIElement, LayoutInfo> _layoutInfo = new HashMap<>();
    private final StackLayoutDrawContext _drawContext;
    private int _width;
    private int _height;

    public StackLayout() {
        _drawContext = new StackLayoutDrawContext(this);
    }

    @Override
    public LayoutInfo getLayoutInfo(UIElement item) {
        return _layoutInfo.get(item);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, QuadRenderer renderer, Position<Float> position,
                     Size<Integer> size) {
        Matrix.translateM(view, 0, position.x(), position.y() + TOP_MARGIN_PX, 0);
        for (var child : _children) {
            child.draw(proj, view, _drawContext, renderer);
        }
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
    }

    @Override
    public int getWidth() {
        return _width;
    }

    @Override
    public int getHeight() {
        return _height;
    }

    private void layout() {
        _layoutInfo.clear();
        var height = 0f;
        for (var child : _children) {
            var size = child.measure();
            _layoutInfo.put(child, new LayoutInfo(0, height));
            height += size.height();
        }
    }

    @Override
    public void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var width = (int) drawContext.widthOf(this);
        var height = (int) drawContext.heightOf(this);

        // TODO: add delta
        draw(0, proj, view, renderer, new Position<>(x, y), new Size<>(width, height));
    }

    @Override
    public Size<Integer> measure() {
        var totalHeight = 0;
        var maxWidth = 0;
        for (var child : _children) {
            var size = child.measure();
            totalHeight += size.height();
            maxWidth = Math.max(maxWidth, size.width());
        }
        return new Size<>(maxWidth, totalHeight);
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

    public void dispose() {
        if (!_disposed) {
            _children.forEach(UIElement::dispose);
            _children.clear();
            _layoutInfo.clear();
            _disposed = true;
        }
    }
}
