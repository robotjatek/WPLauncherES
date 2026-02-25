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
    public void onTouchStart(float x, float y) {

    }

    @Override
    public void onTouchEnd(float x, float y) {

    }

    @Override
    public void onTouchMove(float x, float y) {

    }

    public void clear() {
        _positionedElements.forEach(c -> c._element.dispose());
        _positionedElements.clear();
    }

    @Override
    public void dispose() {
        _positionedElements.forEach(c -> c._element.dispose());
        _positionedElements.clear();
        TileUtil.deleteTexture(_bgTexture);
    }
}