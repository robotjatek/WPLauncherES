package com.robotjatek.wplauncher.ContextMenu;

import com.robotjatek.wplauncher.QuadRenderer;

public interface IMenuItemDrawContext {
    QuadRenderer getRenderer();
    float x(MenuOption item);
    float y(MenuOption item);
    float width(MenuOption item);
    float height(MenuOption item);
}
