package com.robotjatek.wplauncher.InternalApps.Components.Layouts.StackLayout;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.InternalApps.Components.UIElement;
import com.robotjatek.wplauncher.InternalApps.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.InternalApps.Components.Layouts.LayoutInfo;
import com.robotjatek.wplauncher.QuadRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackLayout implements ILayout {
    private final float[] _viewMatrix = new float[16];
    private final QuadRenderer _renderer;
    private final List<UIElement> _children = new ArrayList<>();
    private final Map<UIElement, LayoutInfo> _layoutInfo = new HashMap<>();
    private final StackLayoutDrawContext _drawContext;
    private int _width;
    private int _height;

    public StackLayout(QuadRenderer renderer) {
        _renderer = renderer;
        _drawContext = new StackLayoutDrawContext(this);
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
    public LayoutInfo getLayoutInfo(UIElement item) {
        return _layoutInfo.get(item);
    }

    @Override
    public void draw(float delta, float[] proj) {
        Matrix.setIdentityM(_viewMatrix, 0);
        for (var child : _children) {
            child.draw(proj, _viewMatrix, this);
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
    public QuadRenderer getRenderer() {
        return _renderer;
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

    @Override
    public List<UIElement> getChildren() {
        return _children;
    }
}
