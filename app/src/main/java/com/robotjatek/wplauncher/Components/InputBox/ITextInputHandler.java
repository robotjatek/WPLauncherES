package com.robotjatek.wplauncher.Components.InputBox;

public interface ITextInputHandler {
        void onTextInput(String input);
        void onBackspace();
        void replaceText(int start, int end, String text);
        void onFocusLost();
        void clearText();
        String getText();
        int getCursorPosition();
        void decrementCursorPosition();
        void incrementCursorPosition();
        void setCursorPosition(int position);
}
