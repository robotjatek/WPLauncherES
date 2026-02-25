package com.robotjatek.wplauncher.Components.Button;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

public class Button implements UIElement {

    private final float[] _modelMatrix = new float[16];
    private String _text;
    private Drawable _icon;
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
    public void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_isDirty) {
            // 1 px white border
            TileUtil.deleteTexture(_foreground);
            TileUtil.deleteTexture(_iconTexture);
            _bgTexture = TileUtil.createTextTexture("", w, h, 0, Typeface.NORMAL, Colors.TRANSPARENT, Colors.WHITE, HorizontalAlign.LEFT, VerticalAlign.CENTER);
            _foreground = TileUtil.createTextTexture(_text, w - 1, h - 1, 48, Typeface.BOLD, Colors.WHITE, Colors.BLACK, HorizontalAlign.LEFT, VerticalAlign.CENTER);
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
        renderer.draw(proj, _modelMatrix, _bgTexture);
        var textOffset = 0;
        if (_icon != null) {
            textOffset = h;
        }

        // FG:
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x+4+textOffset, y+4, 0);
        Matrix.scaleM(_modelMatrix, 0, w-8-textOffset, h-8, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _foreground);

        // draw icon
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x+4, y+4, 0);
        Matrix.scaleM(_modelMatrix, 0, h, h-8, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _iconTexture);
    }

    @Override
    public void onTap() {
        if (_onTap != null) {
            _onTap.run();
        }
    }

    @Override
    public Size<Integer> measure() {
        return new Size<>(0, 100); // TODO: configure height
    }

    public void setText(String text) {
        _text = text;
        _isDirty = true;
    }

    public void setIcon(Drawable icon) {
        _icon = icon;
        _isDirty = true;
    }

    @Override
    public void dispose() {
        TileUtil.deleteTexture(_bgTexture);
        TileUtil.deleteTexture(_foreground);
        TileUtil.deleteTexture(_iconTexture);
    }
}
