package com.robotjatek.wplauncher;

import com.robotjatek.wplauncher.Components.InputBox.ITextInputHandler;

public interface IUIContext {
    void requestFocus(ITextInputHandler element);
    void cancelFocus();
}
