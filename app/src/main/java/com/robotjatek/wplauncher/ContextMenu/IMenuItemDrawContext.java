package com.robotjatek.wplauncher.ContextMenu;

import com.robotjatek.wplauncher.QuadRenderer;

public interface IMenuItemDrawContext {
    QuadRenderer getRenderer();
    float xOf(MenuOption item);
    float yOf(MenuOption item);
    float widthOf(MenuOption item);
    float heightOf(MenuOption item);
}
