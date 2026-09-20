package com.robotjatek.wplauncher.Components.Dropdown;

import android.graphics.Typeface;

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

import java.util.function.Consumer;

public class Dropdown implements UIElement, ITouchable {

    private static final int BORDER_SIZE_PX = 4; // TODO: make this DP aware
    private boolean _disposed = false;
    private final TouchHandler _touchHandler = new TouchHandler(this);
    private final AbsoluteLayout _borderLayout = new AbsoluteLayout();
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _label;
    private final Size<Integer> _closedSize;
    private final Size<Integer> _openSize;
    private Size<Integer> _currentSize;
    private boolean _isDirty = true;
    private final IOverlay _overlay; // TODO: this may not be needed at all
    private final Consumer<String> _onChange; // TODO: call this on animation finish
    private boolean _open = false;
    private ILayout _parent;

    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public IState ANIMATION_STATE(Size<Integer> target) {
        return new AnimationState(this, target);
    }

    private IState _state = IDLE_STATE();

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    // TODO: menu elements
    // TODO: show the selected element
    // TODO: its time to implement layout clipping?
    // TODO: what to do on focus loss
    // TODO: the selected label should be in the accent color
    public Dropdown(Size<Integer> size, IOverlay overlay, Consumer<String> onChange) {
        _overlay = overlay;
        _onChange = onChange;
        _closedSize = size;
        _currentSize = size;
        _openSize = new Size<>(_closedSize.width(), _closedSize.height() * 2); // TODO: calculate dynamically based on the size of the content
        _label = new Label("dark", 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _borderLayout.setBgColor(Colors.WHITE);
        _layout.setBgColor(Colors.BLACK);
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
            _layout.onResize(w - BORDER_SIZE_PX * 2, h - BORDER_SIZE_PX * 2);
            _borderLayout.addChild(_layout, new Position<>((float)BORDER_SIZE_PX, (float)BORDER_SIZE_PX));

            var textOffset = 16f; // TODO: DP aware
            _layout.removeChild(_layout);
            _layout.addChild(_label,
                    new Position<>(textOffset, (h - BORDER_SIZE_PX * 2f) / 2f - _label.measure().height() / 2f));
            _isDirty = false;
        }

        _borderLayout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
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

    public boolean getOpen() {
        return _open;
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
            _disposed = true;
        }
    }
}
