package com.robotjatek.wplauncher.Components.Dropdown;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Dropdown.States.IdleState;
import com.robotjatek.wplauncher.Components.Dropdown.States.AnimationState;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Dropdown<TPayload> implements UIElement, ITouchable {
    private final List<TPayload> _model;
    private TPayload _selected;
    private static final int BORDER_SIZE_PX = 4; // TODO: make this DP aware
    private boolean _disposed = false;
    private final TouchHandler _touchHandler = new TouchHandler(this);
    private final AbsoluteLayout _borderLayout = new AbsoluteLayout();
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final List<DropdownContent<TPayload>> _contents = new ArrayList<>();
    private final Size<Integer> _closedSize;
    private final Size<Integer> _openSize;
    private Size<Integer> _currentSize;
    private boolean _isDirty = true;
    private final Consumer<TPayload> _onChange;
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

    // TODO: what to do on focus loss
    // TODO: the selected label should be in the accent color when the box is opened
    public Dropdown(Size<Integer> size, List<TPayload> items, Function<TPayload, String> labelSelector, Consumer<TPayload> onChange) {
        _model = items;
        _onChange = onChange;
        _closedSize = size;
        _currentSize = size;

        var itemHeight = _closedSize.height() - BORDER_SIZE_PX * 2;
        _openSize = new Size<>(_closedSize.width(), itemHeight * items.size() + BORDER_SIZE_PX * 2);

        for (TPayload item : _model) {
            var labelString = labelSelector != null ? labelSelector.apply(item) : item.toString();
            var content = new DropdownContent<>(this, item, labelString);
            _contents.add(content);
        }

        _borderLayout.setBgColor(Colors.WHITE);
        _layout.setBgColor(Colors.BLACK);
        if (!_model.isEmpty()) {
            _selected = _model.get(0);
            if (_onChange != null) {
                _onChange.accept(_model.get(0));
            }
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

        if (_isDirty) {
            var contentWidth = w - BORDER_SIZE_PX * 2;
            var contentHeight = h - BORDER_SIZE_PX * 2;
            var itemHeight = _closedSize.height() - BORDER_SIZE_PX * 2;
            _layout.onResize(contentWidth, contentHeight);

            _layout.removeAll();

            var selectedIndex = _model.isEmpty() ? 0 : Math.max(0, _model.indexOf(_selected));
            var offset = !_open ? selectedIndex * itemHeight : 0f;

            for (var i = 0; i < _contents.size(); i++) {
                var content = _contents.get(i);
                content.setSize(new Size<>(contentWidth, itemHeight));
                var itemY = (i * itemHeight) - offset;
                _layout.addChild(content, new Position<>(0f, itemY));
            }
            _isDirty = false;
        }

        _borderLayout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));

        var innerWidth = w - BORDER_SIZE_PX * 2;
        var innerHeight = h - BORDER_SIZE_PX * 2;
        if (innerWidth > 0 && innerHeight > 0) {
            Matrix.setIdentityM(_modelMatrix, 0);
            Matrix.translateM(_modelMatrix, 0, x + BORDER_SIZE_PX, y + BORDER_SIZE_PX, 0);
            Matrix.scaleM(_modelMatrix, 0, innerWidth, innerHeight, 1);
            Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
            renderer.beginClip(proj, _modelMatrix);
            _layout.draw(delta, proj, view, renderer, new Position<>(x + BORDER_SIZE_PX, y + BORDER_SIZE_PX), new Size<>(innerWidth, innerHeight));
            renderer.endClip();
        }
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
        var selectedIndex = _model.isEmpty() ? -1 : _model.indexOf(_selected);
        if (selectedIndex >= 0 && selectedIndex < _contents.size()) {
            _contents.get(selectedIndex).onPress();
        }
    }

    @Override
    public void onRelease() {
        _layout.setBgColor(Colors.BLACK);
        var selectedIndex = _model.isEmpty() ? -1 : _model.indexOf(_selected);
        if (selectedIndex >= 0 && selectedIndex < _contents.size()) {
            _contents.get(selectedIndex).onRelease();
        }
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
        if (_open != open) {
            _open = open;
            _isDirty = true;
        }
    }

    public boolean isClosed() {
        return !_open;
    }

    public void setSelected(TPayload item) {
        if (_selected != item) {
            _selected = item;
            if (_onChange != null) {
                _onChange.accept(item);
            }
            _isDirty = true;
        }
    }

    public TPayload getSelected() {
        return _selected;
    }

    public void close() {
        changeState(ANIMATION_STATE(_closedSize));
    }

    public Size<Integer> getClosedSize() {
        return _closedSize;
    }

    public Size<Integer> getOpenSize() {
        return _openSize;
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

    @Override
    public void dispose() {
        if (!_disposed) {
            _borderLayout.dispose();
            _layout.dispose();
            for (var content : _contents) {
                content.dispose();
            }
            _contents.clear();
            _disposed = true;
        }
    }
}
