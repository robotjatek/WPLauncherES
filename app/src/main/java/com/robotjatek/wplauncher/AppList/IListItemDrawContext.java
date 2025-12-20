package com.robotjatek.wplauncher.AppList;

import com.robotjatek.wplauncher.QuadRenderer;

public interface IListItemDrawContext {
    QuadRenderer getRenderer();
    float x(ListItem item);
    float y(ListItem item);
    float width(ListItem item);
    float height(ListItem item);
}
