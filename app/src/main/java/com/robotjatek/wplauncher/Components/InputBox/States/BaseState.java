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
        var pos = _context.getCursorPosition();
        replaceText(pos, pos, input);
        _context.setCursorPosition(pos + input.length());
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

    public void replaceText(int start, int end, String text) {
        var base = _context.getText();
        var safeStart = Math.clamp(start, 0, base.length());
        var safeEnd = Math.clamp(end, 0, base.length());
        if (safeStart > safeEnd) {
            var tmp = safeStart;
            safeStart = safeEnd;
            safeEnd = tmp;
        }
        _context.setText(base.substring(0, safeStart) + text + base.substring(safeEnd));
        onTextModified();
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

    public void onFocusLost() {}
}
