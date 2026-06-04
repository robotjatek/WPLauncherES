package com.robotjatek.wplauncher.Components.ScrollView;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Components.ScrollView.States.IdleState;
import com.robotjatek.wplauncher.Components.ScrollView.States.ScrollState;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.ScrollController;

public class ScrollView implements UIElement {

    private final UIElement _child;
    private final ScrollController _scroll = new ScrollController();
    private boolean _disposed = false;
    private final float[] _modelMatrix = new float[16];
    private final float[] _clipMatrix = new float[16];
    private final ScrollViewDrawContext _drawContext = new ScrollViewDrawContext();
    private Size<Integer> _size;
    private boolean _dirty = true;
    private final float _topPadding;
    private final float _bottomPadding;

    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public IState SCROLL_STATE(float y) {
        return new ScrollState(this, y);
    }

    private IState _state = IDLE_STATE();

    public ScrollView(UIElement child, float topPadding, float bottomPadding) {
        _child = child;
        _topPadding = topPadding;
        _bottomPadding = bottomPadding;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _state.update(delta);
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = drawContext.widthOf(this);
        var h = drawContext.heightOf(this);

        if (_dirty) {
            updateScrollBounds(h);
            _dirty = false;
        }

        Matrix.setIdentityM(_clipMatrix, 0);
        Matrix.translateM(_clipMatrix, 0, x, y, 0);
        Matrix.scaleM(_clipMatrix, 0, w, h, 1);
        Matrix.multiplyMM(_clipMatrix, 0, view, 0, _clipMatrix, 0);

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y + _scroll.getScrollOffset(), 0);
        Matrix.multiplyMM(_modelMatrix, 0, _modelMatrix, 0, view, 0);

        renderer.beginClip(proj, _clipMatrix);
        _child.draw(delta, proj, _modelMatrix, _drawContext, renderer);
        renderer.endClip();
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public ScrollController getScroll() {
        return _scroll;
    }

    public void setSize(Size<Integer> size) {
        _size = size;
        _dirty = true;
    }

    @Override
    public Size<Integer> measure() {
        if (_size != null) {
            return _size;
        }

        if (_child == null) {
            throw new RuntimeException("No child was set to the scroll view");
        }
        return _child.measure();
    }

    private void updateScrollBounds(float viewportHeight) {
        var contentHeight = _drawContext.heightOf(_child);
        // The minimum scroll offset is the difference between viewport and content
        // If content is smaller than viewport, min should be 0 (no scrolling)
        var minScroll = Math.min(0, viewportHeight - (contentHeight + _topPadding + _bottomPadding));
        _scroll.setBounds(minScroll, 0);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _child.dispose();
            _disposed = true;
        }
    }

    static class ScrollViewDrawContext implements IDrawContext<UIElement> {

        @Override
        public float xOf(UIElement element) {
            return 0;
        }

        @Override
        public float yOf(UIElement element) {
            return 0;
        }

        @Override
        public float widthOf(UIElement element) {
            return element.measure().width();
        }

        @Override
        public float heightOf(UIElement element) {
            return element.measure().height();
        }
    }
}
