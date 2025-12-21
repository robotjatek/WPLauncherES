package com.robotjatek.wplauncher.AppList;

import com.robotjatek.wplauncher.QuadRenderer;

public interface IListItemDrawContext<T> {
    QuadRenderer getRenderer();
    float xOf(ListItem<T> item);
    float yOf(ListItem<T> item);
    float widthOf(ListItem<T> item);
    float heightOf(ListItem<T> item);
}
