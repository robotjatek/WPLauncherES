package com.robotjatek.wplauncher.Components.InputBox;

public interface ITextInputHandler {
        void onTextInput(String input);
        void onComposingText(String input);
        void onBackspace();
        void onFocus();
        void onFocusLost();
        void clearText();
        String getText();
        int getCursorPosition();
}
