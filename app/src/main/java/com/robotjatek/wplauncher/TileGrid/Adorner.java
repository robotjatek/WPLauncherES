package com.robotjatek.wplauncher.TileGrid;

import android.graphics.drawable.Drawable;

import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Theme.ITheme;

import java.util.function.Supplier;

// TODO: make adorner an UI element
// TODO: make adorner to appear on the overlay instead of baked into the tilegrid
public class Adorner {
    private boolean _disposed = false;
    private final IDrawContext<Adorner> _context;
    private final Runnable _onTap;
    private final Position<Float> _relativePosition;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Icon _icon;
    private final static int ICON_SIZE = 96;
    private final Supplier<ITheme.AdornerStyle> _styleSupplier;
    private ITheme.AdornerStyle _currentStyle;

    public Adorner(Runnable onTap, Drawable icon, Position<Float> relativePosition, IDrawContext<Adorner> context, Supplier<ITheme.AdornerStyle> themeSupplier) {
        _context = context;
        _onTap = onTap;
        _styleSupplier = themeSupplier;
        _currentStyle = themeSupplier.get();
        _relativePosition = relativePosition;

        _icon = new Icon(icon, new Size<>(ICON_SIZE, ICON_SIZE));
        _icon.setTint(_currentStyle.tint());
        _layout.addChild(_icon, Position.ZERO);
    }

    private void syncTheme() {
        var style = _styleSupplier.get();
        if (!style.equals(_currentStyle)) {
            _currentStyle = style;
            _icon.setTint(_currentStyle.tint());
        }
    }

    public void draw(float delta, float[] proj, float[] view, QuadRenderer renderer) {
        var x = _context.xOf(this);
        var y = _context.yOf(this);
        var w = (int) _context.widthOf(this);
        var h = (int) _context.heightOf(this);

        syncTheme();

        if (_icon.measure().width() != w || _icon.measure().height() != h) {
            _icon.setSize(new Size<>(w, h));
        }

        _layout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    public void onTap() {
        _onTap.run();
    }

    public boolean isTapped(float x, float y) {
        var padding = 64;
        var left = _context.xOf(this) - padding;
        var top = _context.yOf(this) - padding;
        var right = left + _context.widthOf(this) + (padding * 2);
        var bottom = top + _context.heightOf(this) + (padding * 2);

        return x >= left && x <= right && y >= top && y <= bottom;
    }

    public Position<Float> getRelativePosition() {
        return _relativePosition;
    }

    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
