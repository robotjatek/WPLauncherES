package com.robotjatek.wplauncher.Components.InputBox;

public interface ITextInputHandler {
        void onTextInput(String newText);
        void onComposingText(String text);
        void onBackspace();
        void onFocus();
        void onFocusLost();
        void clearText();
}
