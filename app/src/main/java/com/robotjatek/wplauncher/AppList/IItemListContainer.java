package com.robotjatek.wplauncher.AppList;

import com.robotjatek.wplauncher.Components.ListPage.ListItem;

import java.util.List;

public interface IItemListContainer<T> {
    List<ListItem<T>> getItems();
}
