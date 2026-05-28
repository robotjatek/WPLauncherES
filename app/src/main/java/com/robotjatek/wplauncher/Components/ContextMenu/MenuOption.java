package com.robotjatek.wplauncher.Components.ContextMenu;

import android.graphics.Typeface;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;
import java.util.function.Consumer;
import java.util.function.Function;

public class MenuOption<T> implements ITouchable {

    private boolean _disposed = false;
    private final Consumer<T> _action;
    private final ContextMenu<T> _menu;
    private final Function<T, Boolean> _isEnabled;
    private Boolean _prevEnabled = null;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _option;
    private boolean _dirty = true;
    private final TouchHandler _touchHandler = new TouchHandler(this);
    private T _payload;

    public MenuOption(String label, Consumer<T> action, ContextMenu<T> menu, Function<T, Boolean> isEnabled) {
        _action = action;
        _menu = menu;
        _isEnabled = isEnabled;
        _option = new Label(label, 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _layout.setBgColor(Colors.CONTEXT_MENU_GRAY);
    }

    public TouchHandler getTouchHandler() {
        return _touchHandler;
    }

    @Override
    public void onPress() {
        if (_isEnabled == null || _isEnabled.apply(_payload)) {
            _layout.setBgColor(Colors.WHITE);
            _dirty = true;
        }
    }

    @Override
    public void onRelease() {
        if (_isEnabled == null || _isEnabled.apply(_payload)) {
            _layout.setBgColor(Colors.CONTEXT_MENU_GRAY);
            _dirty = true;
        }
    }

    @Override
    public void onAction() {
        if (_payload == null) return;
        if (_isEnabled == null || _isEnabled.apply(_payload)) {
            if (_action != null) {
                _action.accept(_payload);
            }
            _menu.close();
        }
    }

    public void draw(float delta, float[] proj, float[] view, QuadRenderer renderer, T payload) {
        _touchHandler.update(delta);
        _payload = payload;
        var x = _menu.xOf(this);
        var y = _menu.yOf(this);
        var w = (int) _menu.widthOf(this);
        var h = (int) _menu.heightOf(this);

        if (_dirty) {
            _layout.removeChild(_option);
            _layout.addChild(_option, new Position<>(w * 0.05f, (h / 2f) - _option.measure().height() / 2f));
            _dirty = false;
        }

        _option.setMaxWidth(w);

        if (_isEnabled != null && payload != null) {
            var isEnabled = _isEnabled.apply(payload);
            if (isEnabled != _prevEnabled) {
                _option.setTextColor(isEnabled ? Colors.WHITE : Colors.LIGHT_GRAY);
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