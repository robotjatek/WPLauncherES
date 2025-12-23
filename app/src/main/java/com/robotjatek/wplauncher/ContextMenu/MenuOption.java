package com.robotjatek.wplauncher.ContextMenu;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.VerticalAlign;
import com.robotjatek.wplauncher.TileUtil;

public class MenuOption {

    private final Runnable _action;
    private final IMenuItemDrawContext _context;

    private final float[] _modelMatrix = new float[16];

    private int _textureId;

    public MenuOption(String label, Runnable action, IMenuItemDrawContext context) {
        _action = action;
        _context = context;
        _textureId = TileUtil.createTextTexture(label,
                (int) context.widthOf(this),
                (int) context.heightOf(this),
                48,
                Typeface.BOLD,
                0xffffffff,
                0xff222222,
                VerticalAlign.CENTER);
    }

    public void onTap() {
        if (_action != null) {
            _action.run();
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
