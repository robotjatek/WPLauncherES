package com.robotjatek.wplauncher;

import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;

public class CustomInputConnection extends BaseInputConnection {

    private final LauncherSurfaceView _targetView;

    public CustomInputConnection(@NonNull LauncherSurfaceView targetView) {
        super(targetView, false);
        _targetView = targetView;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        _targetView.getFocusedInputHandler().onTextInput(text.toString());
        return true;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        _targetView.getFocusedInputHandler().onComposingText(text.toString());
        return true;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        _targetView.getFocusedInputHandler().onBackspace();
        return true;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_DEL) {

            _targetView.getFocusedInputHandler().onBackspace();
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
}
