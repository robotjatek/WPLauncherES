package com.robotjatek.wplauncher.ContextMenu;

import com.robotjatek.wplauncher.QuadRenderer;

public interface IContextMenuDrawContext {
    QuadRenderer getRenderer();
    float xOf(ContextMenu menu);
    float yOf(ContextMenu menu);
    float widthOf(ContextMenu menu);
    float heightOf(ContextMenu menu);
}
