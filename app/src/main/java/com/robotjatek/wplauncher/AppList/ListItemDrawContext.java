package com.robotjatek.wplauncher.AppList;

import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public class ListItemDrawContext<T, U extends IItemListContainer<T>> implements IDrawContext<ListItem<T>> {

    private final int PAGE_PADDING_PX;
    private final int ITEM_HEIGHT_PX;
    private final int ITEM_GAP_PX;
    private int _listWidth;
    private final U _itemContainer;
    private final QuadRenderer _renderer;

    public ListItemDrawContext(int padding, int itemHeight, int itemGap, U itemContainer, QuadRenderer renderer) {
        PAGE_PADDING_PX = padding;
        ITEM_HEIGHT_PX = itemHeight;
        ITEM_GAP_PX = itemGap;
        _itemContainer = itemContainer;
        _renderer = renderer;
    }

    public void onResize(int listWidth) {
        _listWidth = listWidth;
    }

    @Override
    public float xOf(ListItem<T> item) {
        return PAGE_PADDING_PX;
    }

    @Override
    public float yOf(ListItem<T> item) {
        var index = _itemContainer.getItems().indexOf(item);
        if (index == -1) {
            throw new RuntimeException("List item not found");
        }
        return index * (ITEM_HEIGHT_PX + ITEM_GAP_PX);
    }

    @Override
    public float widthOf(ListItem<T> item) {
        return _listWidth - PAGE_PADDING_PX;
    }

    @Override
    public float heightOf(ListItem<T> item) {
        return ITEM_HEIGHT_PX;
    }

    @Override
    public QuadRenderer getRenderer() {
        return _renderer;
    }
}
