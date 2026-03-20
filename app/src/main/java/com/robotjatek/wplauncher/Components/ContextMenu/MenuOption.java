package com.robotjatek.wplauncher.Components.ContextMenu;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.VerticalAlign;
import com.robotjatek.wplauncher.TileUtil;

import java.util.function.Consumer;
import java.util.function.Function;

// TODO: make MenuOption to use labels, and layouts
public class MenuOption<T> {

    private boolean _disposed = false;
    private final Consumer<T> _action;
    private final IDrawContext<MenuOption<T>> _context;
    private final float[] _modelMatrix = new float[16];
    private int _textureId;
    private final Function<T, Boolean> _isEnabled;
    private Boolean _prevEnabled = null;
    private final String _label;

    public MenuOption(String label, Consumer<T> action, IDrawContext<MenuOption<T>> context, Function<T, Boolean> isEnabled) {
        _action = action;
        _context = context;
        _isEnabled = isEnabled;
        _label = label;
        _textureId = TileUtil.createTextTexture(label,
                (int) context.widthOf(this),
                (int) context.heightOf(this),
                48,
                Typeface.BOLD,
                Colors.WHITE,
                Colors.CONTEXT_MENU_GRAY,
                HorizontalAlign.LEFT,
                VerticalAlign.CENTER);
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

    public void draw(float[] proj, float[] view, QuadRenderer renderer, T payload) {
        var w = _context.widthOf(this);
        var h = _context.heightOf(this);

        if (_isEnabled != null) {
            var isEnabled = _isEnabled.apply(payload);
            if (isEnabled != _prevEnabled) {
                var color = isEnabled ? Colors.WHITE : Colors.LIGHT_GRAY;
                TileUtil.deleteTexture(_textureId);
                _textureId = TileUtil.createTextTexture(_label, (int) w, (int) h, 48, Typeface.BOLD,
                        color, Colors.CONTEXT_MENU_GRAY, HorizontalAlign.LEFT, VerticalAlign.CENTER);
                _prevEnabled = isEnabled;
            }
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, _context.xOf(this), _context.yOf(this), 0);
        Matrix.scaleM(_modelMatrix, 0, _context.widthOf(this), _context.heightOf(this), 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _textureId);
    }

    public void dispose() {
        if (!_disposed) {
            TileUtil.deleteTexture(_textureId);
            _textureId = -1;
            _disposed = true;
        }
    }
}
