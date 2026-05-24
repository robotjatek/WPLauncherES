package com.robotjatek.wplauncher.AppList;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.ScrollController;

import java.util.List;

public interface IItemListContainer<T> {
    List<ListItem<T>> getItems();
    List<ListItem<T>> getVisibleItems();
    Size<Integer> getSize();
    ScrollController getScroll();
}
