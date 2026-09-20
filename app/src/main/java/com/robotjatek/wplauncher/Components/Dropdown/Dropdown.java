package com.robotjatek.wplauncher.Components.Dropdown;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Dropdown.States.IdleState;
import com.robotjatek.wplauncher.Components.Dropdown.States.OpeningState;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
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
    private Size<Integer> _openSize; // TODO: calculate dynamically based on the size of the content
    private Size<Integer> _currentSize; // TODO: should be used in animation
    private boolean _isDirty = true;
    private final IOverlay _overlay; // TODO: this may not be needed at all
    private final Consumer<String> _onChange; // TODO: call this on animation finish
    private boolean _open = false;
    private ILayout _parent;

    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public IState OPENING_STATE() {
        return new OpeningState(this);
    }

    private IState _state = IDLE_STATE();

    // TODO: on tap grow the dropdown
    // TODO: menu elements
    // TODO: show the selected element
    // TODO: its time to implement layout clipping?
    // TODO: animated open
    // TODO: what to do on focus loss
    // TODO: the selected should be in the accent color
    public Dropdown(Size<Integer> size, IOverlay overlay, Consumer<String> onChange) {
        _overlay = overlay;
        _onChange = onChange;
        _closedSize = size;
        _currentSize = size;
        _openSize = new Size<>(_closedSize.width(), _closedSize.height() * 2);
        _label = new Label("dark", 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _borderLayout.setBgColor(Colors.WHITE);
        _layout.setBgColor(Colors.BLACK);
        _state.enter();
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _touchHandler.update(delta);
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
    public boolean handleDown(DownGesture gesture) {
        _touchHandler.onDown(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _touchHandler.onUp();
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _touchHandler.onMove(gesture.getX(), gesture.getY());
        return true;
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
        // after touch flash is done trigger open menu
        // TODO: animated grow
        if (_open) {
            setSize(_closedSize);
            _open = false;
        } else {
            setSize(_openSize); // TODO: this is just a quick hack to see if i can grow the border and the content area
            _open = true;
        }
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
