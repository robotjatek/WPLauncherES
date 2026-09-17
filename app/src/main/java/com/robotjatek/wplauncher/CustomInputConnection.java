package com.robotjatek.wplauncher;

import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;

import androidx.annotation.NonNull;

public class CustomInputConnection extends BaseInputConnection {

    private final LauncherSurfaceView _targetView;
    private int _composingStart = -1;
    private int _composingEnd = -1;

    public CustomInputConnection(@NonNull LauncherSurfaceView targetView) {
        super(targetView, false);
        _targetView = targetView;
    }

    public int getComposingStart() {
        return _composingStart;
    }

    public int getComposingEnd() {
        return _composingEnd;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        var handler = _targetView.getFocusedInputHandler();
        if (handler == null) {
            return false;
        }

        int start;
        int end;
        if (_composingStart != -1) {
            start = _composingStart;
            end = _composingEnd;
        } else {
            start = end = handler.getCursorPosition();
        }

        handler.replaceText(start, end, text.toString());

        int newPos;
        if (newCursorPosition > 0) {
            newPos = start + text.length() + newCursorPosition - 1;
        } else {
            newPos = start + newCursorPosition;
        }
        handler.setCursorPosition(Math.clamp(newPos, 0, handler.getText().length()));

        _composingStart = -1;
        _composingEnd = -1;
        return true;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        var handler = _targetView.getFocusedInputHandler();
        if (handler == null) {
            return false;
        }

        int start, end;
        if (_composingStart != -1) {
            start = _composingStart;
            end = _composingEnd;
        } else {
            start = end = handler.getCursorPosition();
        }

        handler.replaceText(start, end, text.toString());

        _composingStart = start;
        _composingEnd = start + text.length();

        int newPos;
        if (newCursorPosition > 0) {
            newPos = _composingEnd + newCursorPosition - 1;
        } else {
            newPos = _composingStart + newCursorPosition;
        }
        handler.setCursorPosition(Math.clamp(newPos, 0, handler.getText().length()));

        return true;
    }

    @Override
    public boolean finishComposingText() {
        _composingStart = -1;
        _composingEnd = -1;
        return super.finishComposingText();
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        _composingStart = start;
        _composingEnd = end;
        return super.setComposingRegion(start, end);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        var handler = _targetView.getFocusedInputHandler();
        if (handler == null) {
            return false;
        }

        if (_composingStart != -1) {
            _composingStart = -1;
            _composingEnd = -1;
        }

        var pos = handler.getCursorPosition();
        var start = Math.max(0, pos - beforeLength);
        var end = Math.min(handler.getText().length(), pos + afterLength);

        if (start != end) {
            handler.replaceText(start, end, "");
            handler.setCursorPosition(start);
        } else if (beforeLength > 0) {
            handler.onBackspace();
        }

        return true;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_DEL) {

            var handler = _targetView.getFocusedInputHandler();
            if (handler != null) {
                handler.onBackspace();
            }
            return true;
        }
        return super.sendKeyEvent(event);
    }

    @Override
    public boolean performEditorAction(int actionCode) {
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            _targetView.cancelFocus();
            _targetView.clearFocus();
            return true;
        }
        return super.performEditorAction(actionCode);
    }

    @Override
    public CharSequence getTextBeforeCursor(int length, int flags) {
        var handler = _targetView.getFocusedInputHandler();
        if (handler == null) {
            return "";
        }

        var text = handler.getText();
        var cursor = Math.min(handler.getCursorPosition(), text.length());
        return text.substring(Math.max(0, cursor - length), cursor);
    }

    @Override
    public CharSequence getTextAfterCursor(int length, int flags) {
        var handler = _targetView.getFocusedInputHandler();
        if (handler == null) {
            return "";
        }

        var text = handler.getText();
        var cursor = Math.min(handler.getCursorPosition(), text.length());
        return text.substring(cursor, Math.min(cursor + length, text.length()));
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        var handler = _targetView.getFocusedInputHandler();
        if (handler == null) {
            return null;
        }

        var text = handler.getText();
        var et = new ExtractedText();
        et.text = text;
        var cursor = handler.getCursorPosition();
        et.selectionStart = cursor;
        et.selectionEnd = cursor;
        et.startOffset = 0;
        return et;
    }

    @Override
    public boolean setSelection(int start, int end) {
        var handler = _targetView.getFocusedInputHandler();
        if (handler == null) {
            return false;
        }

        var target = Math.clamp(start, 0, handler.getText().length());
        handler.setCursorPosition(target);
        return true;
    }
}
