package com.robotjatek.wplauncher.Components.InputBox;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

import java.util.function.Consumer;

// TODO: when refocusing, the keyboard should start with the text
public class InputBox implements UIElement, ITextInputHandler {

    private boolean _disposed = false;
    private boolean _isDirty = true;
    private final float[] _modelMatrix = new float[16];
    private int _foregroundTexture = -1;
    private String _text = "";
    private final String _placeholder;
    private final Consumer<String> _onTextChanged;
    private final Size<Integer> _size = new Size<>(0, 100); // TODO: configurable size

    public InputBox(String placeholder, Consumer<String> onTextChanged) {
        _placeholder = placeholder;
        _onTextChanged = onTextChanged;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_isDirty) {
            TileUtil.deleteTexture(_foregroundTexture);
            if (_text.isEmpty()) {
                _foregroundTexture = TileUtil.createTextTexture(_placeholder, w - 1, h - 1, 48, Typeface.BOLD,
                        Colors.LIGHT_GRAY, Colors.BLACK, HorizontalAlign.LEFT, VerticalAlign.CENTER);
            } else {
                _foregroundTexture = TileUtil.createTextTexture(_text, w - 1, h - 1, 48, Typeface.BOLD,
                        Colors.WHITE, Colors.BLACK, HorizontalAlign.LEFT, VerticalAlign.CENTER);
            }
            _isDirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.drawFlat(proj, _modelMatrix, Colors.WHITE);

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x + 4, y + 4, 0);
        Matrix.scaleM(_modelMatrix, 0, w - 8, h - 8, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _foregroundTexture);
    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        gesture.getUIContext().requestFocus(this);
        return true;
    }


    @Override
    public void onTextInput(String text) {
        _text += text;
        apply();
    }

    @Override
    public void onComposingText(String text) {
        _text = replaceComposing(_text, text);
        apply();
    }

    @Override
    public void onBackspace() {
        if (!_text.isEmpty()) {
            _text = _text.substring(0, _text.length() - 1);
            apply();
        }
    }

    private String replaceComposing(String base, String composing) {
        int i = 0;
        while (i < base.length() &&
                i < composing.length() &&
                base.charAt(i) == composing.charAt(i)) {
            i++;
        }

        return base.substring(0, i) + composing.substring(i);
    }

    private void apply() {
        _onTextChanged.accept(_text);
        _isDirty = true;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            TileUtil.deleteTexture(_foregroundTexture);
            _disposed = true;
        }
    }
}
