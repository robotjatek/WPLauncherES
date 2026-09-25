package com.robotjatek.wplauncher.Components.Dropdown;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
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
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public class DropdownContent<T> implements UIElement, ITouchable {

    private final Dropdown<T> _parent;
    private final T _item;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _label;
    private final TouchHandler _touchHandler = new TouchHandler(this);
    private Size<Integer> _size = new Size<>(-1, -1);
    private ILayout _parentLayout;
    private boolean _disposed = false;
    private boolean _isDirty = true;

    public DropdownContent(Dropdown<T> parent, T item, String label) {
        _parent = parent;
        _item = item;
        _label = new Label(label, 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _layout.setBgColor(Colors.BLACK);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _touchHandler.update(delta);

        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_isDirty) {
            _layout.removeChild(_label);
            _layout.onResize(w, h);
            var textOffset = 16f; // TODO: DP aware
            var labelY = h / 2f - _label.measure().height() / 2f;
            _layout.addChild(_label, new Position<>(textOffset, labelY));
            _isDirty = false;
        }

        _layout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    @Override
    public Size<Integer> measure() {
        if (_size.width() != -1 && _size.height() != -1) {
            return _size;
        }

        return _label.measure();
    }

    public void setSize(Size<Integer> size) {
        if (!_size.equals(size)) {
            _size = size;
            if (_parentLayout != null) {
                _parentLayout.layout();
            }
            _isDirty = true;
        }
    }

    @Override
    public void setParent(ILayout parent) {
        _parentLayout = parent;
    }

    public T getItem() {
        return _item;
    }

    @Override
    public void onPress() {
        _layout.setBgColor(Colors.WHITE);
        _label.setTextColor(Colors.BLACK);
        _isDirty = true;
    }

    @Override
    public void onRelease() {
        _layout.setBgColor(Colors.BLACK);
        _label.setTextColor(Colors.WHITE);
        _isDirty = true;
    }

    @Override
    public void onAction() {
        _parent.setSelected(_item);
        _parent.close();
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
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _label.dispose();
            _disposed = true;
        }
    }
}
