package com.robotjatek.wplauncher.Components.Checkbox;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.R;
import com.robotjatek.wplauncher.Theme.ITheme;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Checkbox implements UIElement {
    private static final int BORDER_SIZE_PX = 4; // TODO: make this DP aware
    private static final int TOGGLE_SIZE = 100;
    private boolean _disposed = false;
    private boolean _state;
    private final Consumer<Boolean> _onChange;
    private boolean _dirty = true;
    private ILayout _parent;
    private Size<Integer> _size = new Size<>(-1, -1);
    private final AbsoluteLayout _layout;
    private final AbsoluteLayout _checkboxBorderLayout;
    private final AbsoluteLayout _checkboxBackground;
    private final Label _label;
    private final Icon _tickIcon;
    private final Supplier<ITheme.CheckboxStyle> _styleSupplier;
    private ITheme.CheckboxStyle _currentStyle;
    private ITheme.LabelStyle _labelStyle;
    private ITheme.LayoutStyle _borderStyle;
    private ITheme.LayoutStyle _bgStyle;

    public Checkbox(String text, boolean initialState, Consumer<Boolean> onChange, Context context, Supplier<ITheme.CheckboxStyle> style) {
        _onChange = onChange;
        _state = initialState;
        _styleSupplier = style;
        _currentStyle = style.get();
        createInternalStyles();
        _tickIcon = new Icon(ContextCompat.getDrawable(context, R.drawable.icon_tick), new Size<>(TOGGLE_SIZE, TOGGLE_SIZE));
        _tickIcon.setTint(_currentStyle.textColor());

        _label = new Label(text, () -> _labelStyle, -1, null);
        _layout = new AbsoluteLayout();
        _checkboxBorderLayout = new AbsoluteLayout(() -> _borderStyle);
        _checkboxBackground = new AbsoluteLayout(() -> _bgStyle);
    }

    private void createInternalStyles() {
        var s = _currentStyle;
        _labelStyle = new ITheme.LabelStyle(s.textSize(), s.typeface(), s.textColor(), Colors.TRANSPARENT);
        _borderStyle = new ITheme.LayoutStyle(s.borderColor());
        _bgStyle = new ITheme.LayoutStyle(s.bgColor());
    }

    private void syncStyle() {
        var s = _styleSupplier.get();
        if (!s.equals(_currentStyle)) {
            _currentStyle = s;
            createInternalStyles();
            _tickIcon.setTint(_currentStyle.textColor());
            _dirty = true;
        }
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        // Available draw space
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);
        var labelWidth = Math.max(1, w - TOGGLE_SIZE - 16);

        syncStyle();
        if (_dirty) {
            _layout.removeChild(_checkboxBorderLayout);
            _layout.addChild(_checkboxBorderLayout, new Position<>(0f, 0f));

            _checkboxBorderLayout.removeChild(_checkboxBackground);
            _checkboxBorderLayout.onResize(TOGGLE_SIZE, TOGGLE_SIZE);
            _checkboxBackground.onResize(TOGGLE_SIZE - BORDER_SIZE_PX * 2, TOGGLE_SIZE - BORDER_SIZE_PX * 2);
            _checkboxBorderLayout.addChild(_checkboxBackground, new Position<>((float)BORDER_SIZE_PX, (float)BORDER_SIZE_PX));

            if (_state) {
                _checkboxBackground.addChild(_tickIcon, Position.ZERO);
            } else {
                _checkboxBackground.removeChild(_tickIcon);
            }

            _layout.removeChild(_label);
            _label.setMaxWidth(labelWidth);
            _layout.addChild(_label, new Position<>(TOGGLE_SIZE + 16f, (h) / 2f - _label.measure().height() / 2f));

            _dirty = false;
        }

        _layout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    @Override
    public Size<Integer> measure() {
        var textWidth = _label.measure().width();
        var size = new Size<>(TOGGLE_SIZE + 16 + textWidth, TOGGLE_SIZE);
        if (!_size.equals(size)) {
            _size = size;
            if (_parent != null) {
                _parent.layout();
            }
        }
        return _size;
    }

    @Override
    public void setParent(ILayout parent) {
        _parent = parent;
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        toggleState();
        return true;
    }

    public void setState(boolean state) {
        if (state != _state) {
            _state = state;
            _dirty = true;
        }
    }

    private void toggleState() {
        _state = !_state;
        _dirty = true;
        if (_onChange != null) {
            _onChange.accept(_state);
        }
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _tickIcon.dispose();
            _disposed = true;
        }
    }
}
