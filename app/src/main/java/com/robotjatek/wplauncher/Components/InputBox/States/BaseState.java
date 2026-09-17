package com.robotjatek.wplauncher.Components.InputBox.States;

import com.robotjatek.wplauncher.Components.InputBox.InputBox;
import com.robotjatek.wplauncher.IState;

public class BaseState implements IState {

    protected final InputBox _context;

    protected BaseState(InputBox context) {
        _context = context;
    }

    @Override
    public void enter() {}

    @Override
    public void exit() {}

    @Override
    public void update(float delta) {}

    public void onTextInput(String input) {
        var text = _context.getText();
        var pos = _context.getCursorPosition();
        _context.setText(text.substring(0, pos) + input + text.substring(pos));
        _context.setCursorPosition(pos + input.length());
        onTextModified();
    }

    public void onComposingText(String text) {
        var base = _context.getText();
        _context.setText(_context.replaceComposing(base, text));
        _context.setCursorPosition(Math.min(_context.getText().length(), text.length()));
        onTextModified();
    }

    public void onBackspace() {
        int pos = _context.getCursorPosition();
        if (pos > 0) {
            var text = _context.getText();
            _context.setText(text.substring(0, pos - 1) + text.substring(pos));
            _context.setCursorPosition(pos - 1);
            onTextModified();
        }
    }

    public void clearText() {
        _context.setText("");
        _context.setCursorPosition(0);
        onTextModified();
    }

    public void decrementCursorPosition() {
        var pos = _context.getCursorPosition();
        if (pos > 0) {
            _context.setCursorPosition(pos - 1);
        }
    }

    public void incrementCursorPosition() {
        var pos = _context.getCursorPosition();
        if (pos < _context.getText().length()) {
            _context.setCursorPosition(pos + 1);
        }
    }

    protected void onTextModified() {
        _context.apply();
    }

    public void onFocus() {}

    public void onFocusLost() {}
}
