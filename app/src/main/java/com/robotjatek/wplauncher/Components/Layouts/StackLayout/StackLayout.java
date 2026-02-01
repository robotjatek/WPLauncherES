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
import java.util.Optional;

public class StackLayout implements ILayout {
    public static final int TOP_MARGIN_PX = 0;
    private final List<UIElement> _children = new ArrayList<>();
    private final Map<UIElement, LayoutInfo> _layoutInfo = new HashMap<>();
    private final StackLayoutDrawContext _drawContext;
    private int _width;
    private int _height;

    private UIElement _tapStartedOn;

    public StackLayout() {
        _drawContext = new StackLayoutDrawContext(this);
    }

    @Override
    public LayoutInfo getLayoutInfo(UIElement item) {
        return _layoutInfo.get(item);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, QuadRenderer renderer, Position<Float> position) {
        Matrix.translateM(view, 0, position.x(), position.y() + TOP_MARGIN_PX, 0);
        for (var child : _children) {
            child.draw(proj, view, this, renderer);
        }
    }

    @Override
    public void onTouchStart(float x, float y) {
        var tappedChild = getTappedChild(x, y);
        tappedChild.ifPresentOrElse(c -> _tapStartedOn = c, () -> _tapStartedOn = null);
    }

    @Override
    public void onTouchEnd(float x, float y) {
        var tappedChild = getTappedChild(x, y);
        tappedChild.ifPresent(t -> {
            if (t == _tapStartedOn) {
                t.onTap();
            }
        });
        _tapStartedOn = null;
    }

    // TODO: this is mostly the same as getTappedTile()
    private Optional<UIElement> getTappedChild(float x, float y) {
        return _children.stream().filter(t -> {
            var scrollPosition = 0;
            var left = _drawContext.xOf(t);
            var top = _drawContext.yOf(t) + scrollPosition + TOP_MARGIN_PX;
            var right = left + _drawContext.widthOf(t);
            var bottom = top + _drawContext.heightOf(t);
            return x >= left && x <= right && y >= top && y <= bottom;
        }).findFirst();
    }

    @Override
    public void onTouchMove(float x, float y) {

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

    public void dispose() {
        _children.forEach(UIElement::dispose);
        _children.clear();
        _layoutInfo.clear();
    }
}
