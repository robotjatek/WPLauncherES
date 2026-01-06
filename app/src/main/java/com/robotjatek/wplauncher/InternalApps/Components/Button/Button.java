package com.robotjatek.wplauncher.InternalApps.Components.Button;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.InternalApps.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.InternalApps.Components.Size;
import com.robotjatek.wplauncher.InternalApps.Components.UIElement;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

public class Button implements UIElement {

    private final float[] _modelMatrix = new float[16];
    private final String _text;
    private final Drawable _icon;
    private final Runnable _onTap;
    private int _bgTexture = -1;
    private int _foreground = -1;
    private int _iconTexture = -1;
    private boolean _isDirty = true;

    public Button(String text, Drawable icon, Runnable onTap) {
        _text = text;
        _icon = icon;
        _onTap = onTap;
    }

    @Override
    public void draw(float[] proj, float[] view, ILayout layout) {
        var x = layout.getContext().xOf(this);
        var y = layout.getContext().yOf(this);
        var w = (int) layout.getContext().widthOf(this);
        var h = (int) layout.getContext().heightOf(this);

        if (_isDirty) {
            // 1 px white border
            _bgTexture = TileUtil.createTextTexture("", w, h, 0, Typeface.NORMAL, Colors.TRANSPARENT, Colors.WHITE, VerticalAlign.CENTER);
            _foreground = TileUtil.createTextTexture(_text, w - 1, h - 1, 48, Typeface.BOLD, Colors.WHITE, Colors.BLACK, VerticalAlign.CENTER);
            if (_icon != null) {
                _iconTexture = BitmapUtil.createTextureFromDrawable(_icon, h, h);
            }
            _isDirty = false;
        }

        // BG:
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        layout.getContext().getRenderer().draw(proj, _modelMatrix, _bgTexture);
        var textOffset = 0;
        if (_icon != null) {
            textOffset = h;
        }

        // FG:
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x+4+textOffset, y+4, 0);
        Matrix.scaleM(_modelMatrix, 0, w-8-textOffset, h-8, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        layout.getContext().getRenderer().draw(proj, _modelMatrix, _foreground);

        // draw icon
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x+4, y+4, 0);
        Matrix.scaleM(_modelMatrix, 0, h, h-8, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        layout.getContext().getRenderer().draw(proj, _modelMatrix, _iconTexture);
    }

    @Override
    public void onTap() {
        if (_onTap != null) {
            _onTap.run();
        }
    }

    @Override
    public Size measure() {
        return new Size(0, 100); // TODO: configure height
    }

    @Override
    public void dispose() {
        TileUtil.deleteTexture(_bgTexture);
        TileUtil.deleteTexture(_foreground);
        TileUtil.deleteTexture(_iconTexture);
    }
}
