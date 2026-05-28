package com.robotjatek.wplauncher.Components.InputBox;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.function.Consumer;

// TODO: when refocusing, the keyboard should start with the text
public class InputBox implements UIElement, ITextInputHandler {

    private static final int BORDER_SIZE_PX = 4;
    private boolean _disposed = false;
    private boolean _isDirty = true;
    private boolean _focused = false;
    private final AbsoluteLayout _borderLayout = new AbsoluteLayout();
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _label;
    private String _text = "";
    private final String _placeholder;
    private final Consumer<String> _onTextChanged;
    private final Size<Integer> _size = new Size<>(0, 100); // TODO: configurable size

    public InputBox(String placeholder, Consumer<String> onTextChanged) {
        _placeholder = placeholder;
        _onTextChanged = onTextChanged;

        _label = new Label(_placeholder, 48, Typeface.BOLD, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
        _borderLayout.setBgColor(Colors.WHITE);
        _layout.setBgColor(Colors.BLACK);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_isDirty) {
            _borderLayout.removeChild(_layout);
            _layout.onResize(w - BORDER_SIZE_PX * 2, h - BORDER_SIZE_PX * 2);
            _borderLayout.addChild(_layout, new Position<>((float) BORDER_SIZE_PX, (float) BORDER_SIZE_PX));
            var textOffset = 16f;
            _layout.removeChild(_label);
            _layout.addChild(_label, new Position<>(textOffset, (h-BORDER_SIZE_PX*2f)/2f - _label.measure().height() / 2f));

            if (_text.isEmpty() && !_focused) {
                _label.setText(_placeholder);
                _label.setTextColor(Colors.LIGHT_GRAY);
            } else {
                _label.setText(_text);
                _label.setTextColor(Colors.WHITE);
            }
            _isDirty = false;
        }

        _borderLayout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
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

    @Override
    public void onFocus() {
        _focused = true;
        _isDirty = true;
    }

    @Override
    public void onFocusLost() {
        _focused = false;
        _isDirty = true;
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
            _borderLayout.dispose();
            _disposed = true;
        }
    }
}
