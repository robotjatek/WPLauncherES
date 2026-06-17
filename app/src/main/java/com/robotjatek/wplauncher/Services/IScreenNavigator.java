package com.robotjatek.wplauncher.Services;

import androidx.annotation.NonNull;

import com.robotjatek.wplauncher.IScreen;

public interface IScreenNavigator {

    /**
     * Pushes a new screen onto the stack and makes it the active screen.
     * The previous screen remains in the stack and can be returned to by popping the current screen.
     * @param screen The screen to be pushed onto the stack. Must not be null.
     */
    void push(@NonNull IScreen screen);

    /**
     * Pops the current screen from the stack. If there are no more screens, does nothing.
     * Disposes the popped screen.
     */
    void pop();
}
