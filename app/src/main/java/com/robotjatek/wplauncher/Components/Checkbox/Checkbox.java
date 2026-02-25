package com.robotjatek.wplauncher.Components.Checkbox;

import android.content.Context;
import android.graphics.Typeface;
import android.opengl.Matrix;

import androidx.core.content.ContextCompat;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.R;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

import java.util.function.Consumer;

public class Checkbox implements UIElement {
    private static final int TOGGLE_SIZE = 100;
    private final String _label;
    private boolean _state;
    private final Consumer<Boolean> _onChange;
    private final float[] _modelMatrix = new float[16];
    private boolean _dirty = true;
    private int _stateTexture = -1;
    private int _labelTexture = -1;
    private final Context _context;

    public Checkbox(String label, boolean initialState, Consumer<Boolean> onChange, Context context) {
        _label = label;
        _onChange = onChange;
        _context = context;
        _state = initialState;
    }

    @Override
    public void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        // Available draw space
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_dirty) {
            TileUtil.deleteTexture(_stateTexture);
            _stateTexture = _state ?
                    BitmapUtil.createTextureFromDrawable(ContextCompat.getDrawable(_context, R.drawable.icon_tick), 100, 100) :
                    BitmapUtil.createTextureFromBitmap(BitmapUtil.createRect(1, 1, 0, Colors.TRANSPARENT));

            TileUtil.deleteTexture(_labelTexture);
            _labelTexture = TileUtil.createTextTexture(_label, w, h, 48, Typeface.NORMAL,
                    Colors.LIGHT_GRAY, Colors.TRANSPARENT, HorizontalAlign.LEFT, VerticalAlign.CENTER);
            _dirty = false;
        }

        if (_state) {
            Matrix.setIdentityM(_modelMatrix, 0);
            Matrix.translateM(_modelMatrix, 0, x, y, 0);
            Matrix.scaleM(_modelMatrix, 0, TOGGLE_SIZE, TOGGLE_SIZE, 0);
            Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
            renderer.draw(proj, _modelMatrix, _stateTexture);
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x + TOGGLE_SIZE, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w - TOGGLE_SIZE, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _labelTexture);
    }

    @Override
    public Size<Integer> measure() {
        return new Size<>(0, TOGGLE_SIZE);
    }

    @Override
    public void onTap() {
        toggleState();
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
        TileUtil.deleteTexture(_stateTexture);
        TileUtil.deleteTexture(_labelTexture);
    }
}
