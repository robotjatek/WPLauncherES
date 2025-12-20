package com.robotjatek.wplauncher.ContextMenu;

import com.robotjatek.wplauncher.QuadRenderer;

public interface IContextMenuDrawContext {
    QuadRenderer getRenderer();
    float x(ContextMenu item);
    float y(ContextMenu item);
    float width(ContextMenu item); // TODO: ez lehet nem kell
}
