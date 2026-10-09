package com.robotjatek.wplauncher.Components.ListPage;

import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Theme.ITheme;
import com.robotjatek.wplauncher.TileGrid.Position;


import java.util.List;
import java.util.function.Supplier;

/**
 * Wraps the ListView into a full screen list component
 * @param <T> The type of the payload
 */
public class ListPage<T> {
    private boolean _disposed = false;
    private static final int PAGE_PADDING_PX = 60;
    private Size<Integer> _size = new Size<>(-1, -1);
    private final StackLayout _layout;
    private final ListView<T> _listView;

    public ListPage(Supplier<ITheme> themeSupplier) {
        _layout = new StackLayout(() -> themeSupplier.get().layout());
        _listView = new ListView<>(LauncherRenderer.SCREEN_DATA.topInset, LauncherRenderer.SCREEN_DATA.bottomInset, PAGE_PADDING_PX, themeSupplier);
        _listView.setClipping(false);
        _layout.addChild(_listView);
    }

    public List<ListItem<T>> getItems() {
        return _listView.getItems();
    }

    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer) {
        _layout.draw(delta, projMatrix, viewMatrix, renderer, Position.ZERO, _size);
    }

    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    public void onSizeChanged(int width, int height) {
        _size = new Size<>(width, height);
        var listWidth = width - 2 * PAGE_PADDING_PX;
        var topInset = LauncherRenderer.SCREEN_DATA.topInset;
        var bottomInset = LauncherRenderer.SCREEN_DATA.bottomInset;
        _listView.setSize(new Size<>(listWidth, height));
        _listView.setMargins(topInset, bottomInset);
        _layout.onResize(width, height);
    }

    public void resetState() {
        _listView.resetState();
    }

    public void addItem(int index, ListItem<T> item) {
          _listView.addItem(index, item);
    }

    public void addItems(List<ListItem<T>> items) {
        _listView.addItems(items);
    }

    public void removeItem(ListItem<T> item) {
        _listView.removeItemByPayload(item.getPayload());
    }

    public void setContextMenu(ContextMenu<T> menu) {
        _listView.setContextMenu(menu);
    }

    public void resetScroll() {
        _listView.getScroll().setScrollOffset(0);
    }

    public boolean isCatchingGestures() {
        return _listView.isCatchingGestures();
    }

    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
