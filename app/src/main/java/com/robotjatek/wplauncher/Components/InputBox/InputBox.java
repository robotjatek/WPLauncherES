package com.robotjatek.wplauncher.Components.InputBox;

import android.graphics.Paint;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IOverlay;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.function.Consumer;

public class InputBox implements UIElement, ITextInputHandler {

    private static final int BORDER_SIZE_PX = 4; // TODO: make this density aware
    private static final float TEXT_OFFSET = 16f; // Offset from the border of the input box // TODO: this is a hard-coded offset. Make it density aware
    private boolean _disposed = false;
    private boolean _isDirty = true;
    private boolean _focused = false;
    private final AbsoluteLayout _borderLayout = new AbsoluteLayout();
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Cursor _cursor = new Cursor();
    private final CursorHandle _handle = new CursorHandle(this);
    private final Label _label;
    private String _text = "";
    private int _cursorPosition = 0;
    private float _textStartX = 0;
    private final static int BLINK_TIMEOUT = 500; // ms
    private float _blinkTimer = BLINK_TIMEOUT;
    private boolean _cursorVisible = true;
    private final String _placeholder;
    private final Consumer<String> _onTextChanged;
    private final Size<Integer> _size = new Size<>(0, 100); // TODO: configurable size
    private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final IOverlay _overlay;

    public InputBox(String placeholder, Consumer<String> onTextChanged, IOverlay overlay) {
        _placeholder = placeholder;
        _onTextChanged = onTextChanged;
        _overlay = overlay;

        _label = new Label(_placeholder, 48, Typeface.BOLD, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
        _paint.setTypeface(_label.getTypeFace());
        _paint.setTextSize(_label.getTextSize());
        _borderLayout.setBgColor(Colors.WHITE);
        _layout.setBgColor(Colors.BLACK);
        _layout.addChild(_cursor, new Position<>(0f, 0f));
        _cursor.setSize(new Size<>(6, _label.measure().height())); // TODO: density aware: Math.max(2, Math.round(2f * density)); // 2dp wide
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        blinkCursor(delta);

        if (_isDirty) {
            _borderLayout.removeChild(_layout);
            var borderPosition = new Position<>((float) BORDER_SIZE_PX, (float) BORDER_SIZE_PX);
            _layout.onResize(w - BORDER_SIZE_PX * 2, h - BORDER_SIZE_PX * 2);
            _borderLayout.addChild(_layout, borderPosition);
            _textStartX = x + BORDER_SIZE_PX + TEXT_OFFSET;
            _layout.removeChild(_label);
            _layout.addChild(_label, new Position<>(TEXT_OFFSET, (h-BORDER_SIZE_PX*2f)/2f - _label.measure().height() / 2f));

            if (_text.isEmpty() && !_focused) {
                _label.setText(_placeholder);
                _label.setTextColor(Colors.LIGHT_GRAY);
                _cursor.setVisible(false);
            } else {
                var cursorPosition = new Position<>(TEXT_OFFSET + measureTextAtCursorPosition(_text),
                        (h-BORDER_SIZE_PX*2f)/2f - _label.measure().height() / 2f);
                _layout.setChildPosition(_cursor, cursorPosition);
                _label.setText(_text);
                _label.setTextColor(Colors.WHITE);

                var p = new Position<>(x + BORDER_SIZE_PX + cursorPosition.x() - _handle.measure().width() / 2f, y + h);
                if (_overlay != null) {
                    _overlay.setAdornerAt(_handle, p);
                }
            }
            _isDirty = false;
        }

        _borderLayout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    private void blinkCursor(float delta) {
        if (_focused) {
            _blinkTimer += delta;
            if (_blinkTimer > BLINK_TIMEOUT) {
                _cursorVisible = !_cursorVisible;
                _blinkTimer = 0f;
                _cursor.setVisible(_cursorVisible);
            }
        }
    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        if (!_focused) {
            // TODO: register cursor handle here?
            // TODO: show the handle on second tap only
            gesture.getUIContext().requestFocus(this);
        }
        return true;
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        if (_focused) {
            setCursorPosition(gesture.getX());
            return true;
        }
        return false;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        if (_focused) {
            setCursorPosition(gesture.getX());
            return true;
        }
        return false;
    }

    public void setCursorPosition(float x) {
        var insideX = x - _textStartX;
        if (insideX <= 0) {
            _cursorPosition = 0;
            _isDirty = true;
            return;
        }

        for (var i = 0; i < _text.length(); i++) {
            var measured = _paint.measureText(_text, 0, i);
            if (measured >= insideX) {
                var prev = i > 0 ? _paint.measureText(_text, 0, i - 1) : 0;
                _cursorPosition = (insideX - prev < measured - insideX) ? i - 1 : i;
                _isDirty = true;
                return;
            }
        }

        // text is shorter than the tap position
        _cursorPosition = _text.length();
        _isDirty = true;
    }

    private float measureTextAtCursorPosition(String text) {
        return _paint.measureText(text, 0, _cursorPosition);
    }

    @Override
    public void onTextInput(String input) {
        _text = _text.substring(0, _cursorPosition) + input + _text.substring(_cursorPosition);
        _cursorPosition += input.length();
        apply();
    }

    @Override
    public void onComposingText(String text) {
        _text = replaceComposing(_text, text);
        _cursorPosition = Math.min(_text.length(), text.length());
        apply();
    }

    @Override
    public void onBackspace() {
        if (_cursorPosition > 0) {
            _text = _text.substring(0, _cursorPosition - 1) + _text.substring(_cursorPosition);
            _cursorPosition--;
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

    @Override
    public void clearText() {
        _text = "";
        _cursorPosition = 0;
        apply();
    }

    @Override
    public String getText() {
        return _text;
    }

    @Override
    public int getCursorPosition() {
        return _cursorPosition;
    }

    @Override
    public void decrementCursorPosition() {
        _cursorPosition--;
        if (_cursorPosition < 0) {
            _cursorPosition = 0;
        }
    }

    @Override
    public void incrementCursorPosition() {
        _cursorPosition++;
        if (_cursorPosition > _text.length()) {
            _cursorPosition = _text.length();
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
        _blinkTimer = 0;
        _cursorVisible = true;
        _cursor.setVisible(true);
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
