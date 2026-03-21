package com.robotjatek.wplauncher.Components.ContextMenu;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.function.Consumer;
import java.util.function.Function;

public class MenuOption<T> {

    private boolean _disposed = false;
    private final Consumer<T> _action;
    private final IDrawContext<MenuOption<T>> _context;
    private final Function<T, Boolean> _isEnabled;
    private Boolean _prevEnabled = null;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _option;
    private boolean _dirty = true;

    public MenuOption(String label, Consumer<T> action, IDrawContext<MenuOption<T>> context, Function<T, Boolean> isEnabled) {
        _action = action;
        _context = context;
        _isEnabled = isEnabled;

        _option = new Label(label, 48, Typeface.BOLD, Colors.WHITE, Colors.CONTEXT_MENU_GRAY);
        _layout.setBgColor(Colors.CONTEXT_MENU_GRAY);
    }

    public void onTap(T payload) {
        if (_isEnabled == null && _action != null) {
            _action.accept(payload);
            return;
        }

        if (_isEnabled != null && _action != null && _isEnabled.apply(payload)) {
            _action.accept(payload);
        }
    }

    public void draw(float delta, float[] proj, float[] view, QuadRenderer renderer, T payload) {
        var x = _context.xOf(this);
        var y = _context.yOf(this);
        var w = (int) _context.widthOf(this);
        var h = (int) _context.heightOf(this);

        if (_dirty) {
            _layout.addChild(_option, new Position<>(w * 0.05f, (h / 2f) - _option.measure().height() / 2f));
            _dirty = false;
        }

        _option.setMaxWidth(w);

        if (_isEnabled != null) {
            var isEnabled = _isEnabled.apply(payload);
            if (isEnabled != _prevEnabled) {
                var color = isEnabled ? Colors.WHITE : Colors.LIGHT_GRAY;
                _option.setTextColor(color);
                _prevEnabled = isEnabled;
            }
        }

        _layout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
