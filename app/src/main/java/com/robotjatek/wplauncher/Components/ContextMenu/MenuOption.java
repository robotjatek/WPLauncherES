package com.robotjatek.wplauncher.Components.ContextMenu;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.VerticalAlign;
import com.robotjatek.wplauncher.TileUtil;

import java.util.function.Consumer;

public class MenuOption<T> {

    private final Consumer<T> _action;
    private final IDrawContext<MenuOption<T>> _context;
    private final float[] _modelMatrix = new float[16];
    private int _textureId;

    public MenuOption(String label, Consumer<T> action, IDrawContext<MenuOption<T>> context) {
        _action = action;
        _context = context;
        _textureId = TileUtil.createTextTexture(label,
                (int) context.widthOf(this),
                (int) context.heightOf(this),
                48,
                Typeface.BOLD,
                Colors.WHITE,
                Colors.CONTEXT_MENU_GRAY,
                VerticalAlign.CENTER);
    }

    public void onTap(T payload) {
        if (_action != null) {
            _action.accept(payload);
        }
    }

    public void draw(float[] proj, float[] view) {
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, _context.xOf(this), _context.yOf(this), 0);
        Matrix.scaleM(_modelMatrix, 0, _context.widthOf(this), _context.heightOf(this), 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        _context.getRenderer().draw(proj, _modelMatrix, _textureId);
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        _textureId = -1;
    }
}
