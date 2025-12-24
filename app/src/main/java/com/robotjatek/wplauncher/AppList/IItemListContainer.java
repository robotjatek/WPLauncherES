package com.robotjatek.wplauncher.AppList;

import java.util.List;

public interface IItemListContainer<T> {
    List<ListItem<T>> getItems();
}
