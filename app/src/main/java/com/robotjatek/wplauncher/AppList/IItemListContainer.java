package com.robotjatek.wplauncher.AppList;

import com.robotjatek.wplauncher.InternalApps.Components.List.ListItem;

import java.util.List;

public interface IItemListContainer<T> {
    List<ListItem<T>> getItems();
}
