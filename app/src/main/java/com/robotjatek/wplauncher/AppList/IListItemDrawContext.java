package com.robotjatek.wplauncher.AppList;

import com.robotjatek.wplauncher.QuadRenderer;

public interface IListItemDrawContext {
    QuadRenderer getRenderer();
    float xOf(ListItem item);
    float yOf(ListItem item);
    float widthOf(ListItem item);
    float heightOf(ListItem item);
}
