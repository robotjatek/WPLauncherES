package com.robotjatek.wplauncher.Components.Dropdown;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Dropdown.States.IdleState;
import com.robotjatek.wplauncher.Components.Dropdown.States.AnimationState;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IOverlay;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Dropdown<T> implements UIElement, ITouchable {
    private final List<T> _model;
    private T _selected;
    private static final int BORDER_SIZE_PX = 4; // TODO: make this DP aware
    private boolean _disposed = false;
    private final TouchHandler _touchHandler = new TouchHandler(this);
    private final AbsoluteLayout _borderLayout = new AbsoluteLayout();
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final List<Label> _labels = new ArrayList<>();
    private final Size<Integer> _closedSize;
    private final Size<Integer> _openSize;
    private Size<Integer> _currentSize;
    private boolean _isDirty = true;
    private final IOverlay _overlay; // TODO: this may not be needed at all
    private final Consumer<T> _onChange;
    private boolean _open = false;
    private final float[] _modelMatrix = new float[16];
    private ILayout _parent;

    public IState IDLE_STATE() {
        return new IdleState<>(this);
    }

    public IState ANIMATION_STATE(Size<Integer> target) {
        return new AnimationState<>(this, target);
    }

    private IState _state = IDLE_STATE();

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    // TODO: show the selected element
    // TODO: its time to implement layout clipping?
    // TODO: what to do on focus loss
    // TODO: the selected label should be in the accent color
    public Dropdown(Size<Integer> size, List<T> items, Function<T, String> labelSelector, IOverlay overlay, Consumer<T> onChange) {
        _model = items;
        _overlay = overlay;
        _onChange = onChange;
        _closedSize = size;
        _currentSize = size;

        _openSize = new Size<>(_closedSize.width(), _closedSize.height() * items.size()); // TODO: calculate dynamically based on the size of the content

        for(T item : _model) {
            var labelString = labelSelector != null ? labelSelector.apply(item) : item.toString();
            var label = new Label(labelString, 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT, -1, () -> {
                _selected = item;
                _onChange.accept(item);
                changeState(ANIMATION_STATE(_closedSize));
            });
            _labels.add(label);
        }

        _borderLayout.setBgColor(Colors.WHITE);
        _layout.setBgColor(Colors.BLACK);
        if (!_model.isEmpty()) {
            _onChange.accept(_model.get(0));
        }
        _state.enter();
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _state.update(delta);

        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if(_isDirty) {
            _isDirty = false;
            _borderLayout.removeChild(_layout);
            var contentSize = new Size<>(w - BORDER_SIZE_PX * 2, h - BORDER_SIZE_PX * 2);
            var itemHeight = _closedSize.height();
            var firstLineCenter = itemHeight / 2f;
            _layout.onResize(contentSize.width(), contentSize.height());
            _borderLayout.addChild(_layout, new Position<>((float)BORDER_SIZE_PX, (float)BORDER_SIZE_PX));

            var textOffset = 16f; // TODO: DP aware
            _layout.removeAll();
            for (var i = 0; i < _labels.size(); i++) {
                var label = _labels.get(i);
                var labelY = (i * itemHeight) + firstLineCenter - label.measure().height() / 2f;
                _layout.addChild(label, new Position<>(textOffset, labelY));
            }
            _borderLayout.addChild(_layout, new Position<>((float)BORDER_SIZE_PX, (float)BORDER_SIZE_PX));
            _isDirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 1);
        renderer.beginClip(proj, _modelMatrix);
        _borderLayout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
        renderer.endClip();
    }

    @Override
    public Size<Integer> measure() {
        return _currentSize;
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
    }

    @Override
    public void onPress() {
        _layout.setBgColor(Colors.WHITE);
    }

    @Override
    public void onRelease() {
        _layout.setBgColor(Colors.BLACK);
    }

    @Override
    public void onAction() {
        // touch animation already happened => trigger open/close animation
        if (_open) {
            changeState(ANIMATION_STATE(_closedSize));
        } else {
            changeState(ANIMATION_STATE(_openSize));
        }
    }

    public void setOpen(boolean open) {
        _open = open;
    }

    public boolean iClosed() {
        return !_open;
    }

    public ILayout getContentLayout() {
        return _layout;
    }

    public void setSize(Size<Integer> size) {
        if (!_currentSize.equals(size)) {
            _currentSize = size;
            if (_parent != null) {
                _parent.layout();
            }
            _isDirty = true;
        }
    }

    @Override
    public void setParent(ILayout parent) {
        _parent = parent;
    }

    public TouchHandler getTouchhandler() {
        return _touchHandler;
    }

    public T getSelected() {
        return _selected;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _borderLayout.dispose();
            _disposed = true;
        }
    }
}
