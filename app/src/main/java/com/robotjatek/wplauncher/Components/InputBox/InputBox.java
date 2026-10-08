package com.robotjatek.wplauncher.Components.InputBox;

import android.content.Context;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.InputBox.States.ActiveState;
import com.robotjatek.wplauncher.Components.InputBox.States.BaseState;
import com.robotjatek.wplauncher.Components.InputBox.States.IdleState;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.IUIContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.R;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IOverlay;
import com.robotjatek.wplauncher.Theme.ITheme;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class InputBox implements UIElement, ITextInputHandler {

    private static final int BORDER_SIZE_PX = 4; // TODO: make this density aware
    private static final float TEXT_OFFSET = 16f; // Offset from the border of the input box // TODO: this is a hard-coded offset. Make it density aware
    private boolean _disposed = false;
    private boolean _isDirty = true;
    private final AbsoluteLayout _borderLayout;
    private final AbsoluteLayout _layout;
    private int _cursorPosition = 0;
    private boolean _showCursor = false;
    private boolean _showHandle = false;
    private final Cursor _cursor;
    private final CursorHandle _handle;
    private final Label _label;
    private String _text = "";
    private float _textStartX = 0;
    private final String _placeholder;
    private final Consumer<String> _onTextChanged;
    private final Size<Integer> _size = new Size<>(0, 100); // TODO: configurable size
    private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final IOverlay _overlay;
    private ILayout _parent;
    private final Supplier<ITheme.InputBoxStyle> _styleSupplier;
    private ITheme.InputBoxStyle _currentStyle;
    private ITheme.LabelStyle _labelStyle;
    private ITheme.LayoutStyle _borderStyle;
    private ITheme.LayoutStyle _bgStyle;

    public BaseState IDLE_STATE() {
        return new IdleState(this);
    }
    public BaseState ACTIVE_STATE(IUIContext uiContext) { return new ActiveState(this, uiContext); }
    private BaseState _state = IDLE_STATE();

    public InputBox(String placeholder, Consumer<String> onTextChanged, IOverlay overlay, Supplier<ITheme.InputBoxStyle> style, Context context) {
        _placeholder = placeholder;
        _onTextChanged = onTextChanged;
        _overlay = overlay;
        _styleSupplier = style;
        _currentStyle = style.get();
        createInternalStyles();

        _cursor = new Cursor(() -> _currentStyle.cursor());

        var handleIcon = ContextCompat.getDrawable(context, R.drawable.ic_cursor_handle);
        _handle = new CursorHandle(this, handleIcon);

        _label = new Label(_placeholder, () -> _labelStyle);
        _paint.setTypeface(_label.getTypeFace());
        _paint.setTextSize(_label.getTextSize());
        _borderLayout = new AbsoluteLayout(() -> _borderStyle);
        _layout = new AbsoluteLayout(() -> _bgStyle);
        _layout.addChild(_cursor, new Position<>(0f, 0f));
        _cursor.setSize(new Size<>(6, _label.measure().height())); // TODO: density aware: Math.max(2, Math.round(2f * density)); // 2dp wide

        updateLabel();
        _state.enter();
    }

    private void createInternalStyles() {
        var s = _currentStyle;
        var textColor = _text.isEmpty() ? s.placeholderColor() : s.textColor();
        _labelStyle = new ITheme.LabelStyle(s.textSize(), s.typeface(), textColor, Colors.TRANSPARENT);
        _borderStyle = new ITheme.LayoutStyle(s.borderColor());
        _bgStyle = new ITheme.LayoutStyle(s.bgColor());
    }

    private void syncTheme() {
        var style = _styleSupplier.get();
        if (!style.equals(_currentStyle)) {
            _currentStyle = style;
            createInternalStyles();
            _isDirty = true;
        }
    }

    public void changeState(BaseState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _state.update(delta);
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        syncTheme();
        if (_isDirty) {
            _borderLayout.removeChild(_layout);
            var borderPosition = new Position<>((float) BORDER_SIZE_PX, (float) BORDER_SIZE_PX);
            _layout.onResize(w - BORDER_SIZE_PX * 2, h - BORDER_SIZE_PX * 2);
            _borderLayout.addChild(_layout, borderPosition);
            _textStartX = x + BORDER_SIZE_PX + TEXT_OFFSET;
            _layout.removeChild(_label);
            _layout.addChild(_label, new Position<>(TEXT_OFFSET, (h-BORDER_SIZE_PX*2f)/2f - _label.measure().height() / 2f));

            var cursorPosition = new Position<>(TEXT_OFFSET + measureTextAtCursorPosition(_text),
                    (h-BORDER_SIZE_PX*2f)/2f - _label.measure().height() / 2f);
            if (_showCursor) {
                _layout.setChildPosition(_cursor, cursorPosition);
            }

            if (_overlay != null && _showHandle) {
                var p = new Position<>(x + BORDER_SIZE_PX + cursorPosition.x() - _handle.measure().width() / 2f, y + h);
                _overlay.setAdornerAt(_handle, p);
            }

            _isDirty = false;
        }
        _borderLayout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    public void showCursor(boolean show) {
        _showCursor = show;
        _cursor.setVisible(show);
        _isDirty = true;
    }

    public void showHandle(boolean show) {
        _showHandle = show;
        _handle.setVisible(show);
        if (!show){
            _overlay.removeAdorner(_handle);
        }
        _isDirty = true;
    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
    }

    public void setCursorPositionWithXPosition(float x) {
        setCursorPosition(calculateCursorPositionOnX(x));
    }

    public int calculateCursorPositionOnX(float x) {
        var insideX = x - _textStartX;
        if (insideX <= 0) {
            return 0;
        }

        for (var i = 0; i < _text.length(); i++) {
            var measured = _paint.measureText(_text, 0, i);
            if (measured >= insideX) {
                var prev = i > 0 ? _paint.measureText(_text, 0, i - 1) : 0;
                return (insideX - prev < measured - insideX) ? i - 1 : i;
            }
        }

        return _text.length(); // text is shorter than the tap position
    }

    public float getCursorXOnCurrentPosition() {
        return _textStartX + measureTextAtCursorPosition(_text);
    }

    public float measureTextAtCursorPosition(String text) {
        return _paint.measureText(text, 0, _cursorPosition);
    }

    @Override
    public void onTextInput(String input) {
        _state.onTextInput(input);
    }

    @Override
    public void onBackspace() {
        _state.onBackspace();
    }

    @Override
    public void replaceText(int start, int end, String text) {
        _state.replaceText(start, end, text);
    }

    @Override
    public void onFocusLost() {
        _state.onFocusLost();
    }

    @Override
    public void clearText() {
        _state.clearText();
    }

    @Override
    public String getText() {
        return _text;
    }

    public void setText(String text) {
        this._text = text;
        updateLabel();
    }

    @Override
    public int getCursorPosition() {
        return _cursorPosition;
    }

    @Override
    public void setCursorPosition(int position) {
        _cursorPosition = position;
        _isDirty = true;
    }

    @Override
    public void decrementCursorPosition() {
        _state.decrementCursorPosition();
    }

    @Override
    public void incrementCursorPosition() {
        _state.incrementCursorPosition();
    }

    public void apply() {
        updateLabel();
        _onTextChanged.accept(_text);
        _isDirty = true;
    }

    public IOverlay getOverlay() {
        return _overlay;
    }

    private void updateLabel() {
        if (_text.isEmpty()) {
            _label.setText(_placeholder);
        } else {
            _label.setText(_text);
        }
        createInternalStyles();
    }

    @Override
    public void setParent(ILayout parent) {
        _parent = parent;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _borderLayout.dispose();
            _handle.dispose();
            _cursor.dispose();
            _disposed = true;
        }
    }
}
