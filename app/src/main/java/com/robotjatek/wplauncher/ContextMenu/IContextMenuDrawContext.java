package com.robotjatek.wplauncher.ContextMenu;

import com.robotjatek.wplauncher.QuadRenderer;

public interface IContextMenuDrawContext {
    QuadRenderer getRenderer();
    float x(ContextMenu menu);
    float y(ContextMenu menu);
    float width(ContextMenu menu);
    float height(ContextMenu menu);
}
