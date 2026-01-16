package com.robotjatek.wplauncher.Components.List;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.IItemListContainer;
import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenuDrawContext;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.Components.List.States.ContextMenuState;
import com.robotjatek.wplauncher.Components.List.States.IdleState;
import com.robotjatek.wplauncher.Components.List.States.ScrollState;
import com.robotjatek.wplauncher.Components.List.States.TappedState;
import com.robotjatek.wplauncher.Components.List.States.TouchingState;
import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.ScrollController;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.ArrayList;
import java.util.List;

public class ListView<T> implements IItemListContainer<T>, Page {
    public IState IDLE_STATE() {
        return new IdleState<>(this);
    }

    public IState TOUCHING_STATE(float x, float y) {
        return new TouchingState<>(this, x, y);
    }

    public IState SCROLL_STATE(float y) {
        return new ScrollState<>(this, y);
    }

    public IState TAPPED_STATE(float y) {
        return new TappedState<>(this, y);
    }

    public IState CONTEXT_MENU_STATE(float x, float y) {
        return new ContextMenuState<>(this, x, y);
    }

    private IState _state = IDLE_STATE();
    public static final int TOP_MARGIN_PX = 0;
    public static final int ITEM_HEIGHT_PX = 128;
    public static final int ITEM_GAP_PX = 5;
    private static final int PAGE_PADDING_PX = 60;
    public static final float BOTTOM_MARGIN_PX = 240;
    private final float[] scrollMatrix = new float[16];
    private final ScrollController _scroll = new ScrollController();
    private final ListItemDrawContext<T, ListView<T>> _drawContext;
    private final ContextMenuDrawContext<T> _contextMenuDrawContext;
    private final List<ListItem<T>> _items = new ArrayList<>();
    private int _viewPortHeight;
    private ContextMenu<T> _contextMenu;

    public ListView(QuadRenderer renderer) {
        _drawContext = new ListItemDrawContext<>(PAGE_PADDING_PX, ITEM_HEIGHT_PX, ITEM_GAP_PX, this, renderer);
        _contextMenuDrawContext = new ContextMenuDrawContext<>(0, _viewPortHeight, renderer);
    }

    @Override
    public List<ListItem<T>> getItems() {
        return _items;
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        _state.update(delta);
        Matrix.setIdentityM(scrollMatrix, 0);
        Matrix.translateM(scrollMatrix, 0, 0, _scroll.getScrollOffset() + TOP_MARGIN_PX, 0);
        Matrix.multiplyMM(scrollMatrix, 0, scrollMatrix, 0, viewMatrix, 0);

        for (var i : _items) {
            i.update(_drawContext);
            i.draw(projMatrix, scrollMatrix, _drawContext);
        }

        // Draw the context menu last so it shows up above everything else
        if (_contextMenu != null && _contextMenu.isOpened()) {
            _contextMenu.draw(projMatrix, viewMatrix);
        }
    }

    @Override
    public void touchMove(float x, float y) {
        _state.handleMove(x, y);
    }

    @Override
    public void touchStart(float x, float y) {
        _state.handleTouchStart(x, y);
    }

    @Override
    public void touchEnd(float x, float y) {
        _state.handleTouchEnd(x, y);
    }

    @Override
    public void onSizeChanged(int width, int height) {
        _viewPortHeight = height;
        _drawContext.onResize(width);
        var _listWidth = width - 2 * PAGE_PADDING_PX;
        _contextMenuDrawContext.onResize(_listWidth, height);
        _items.forEach(ListItem::setDirty);
        setScrollBounds();
    }

    public ListItemDrawContext<T, ListView<T>> getDrawContext() {
        return _drawContext;
    }

    public void addItems(List<ListItem<T>> items) {
        _items.addAll(items);
        _items.forEach(ListItem::setDirty);
        setScrollBounds();
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public ScrollController getScroll() {
        return _scroll;
    }

    public ContextMenu<T> openContextMenu(float x, float y, T item) {
        if (_contextMenu != null) {
            _contextMenu.open(new Position(x, y), item);
        }
        return _contextMenu;
    }

    public void closeContextMenu() {
        if (_contextMenu != null) {
            _contextMenu.close();
        }
    }

    public void setContextMenu(ContextMenu<T> menu) {
        if (_contextMenu != null) {
            _contextMenu.dispose();
        }
        _contextMenu = menu;
    }

    public boolean hasContextMenu() {
        return _contextMenu != null;
    }

    private void setScrollBounds() {
        var contentHeight = _items.size() * (ITEM_HEIGHT_PX + ITEM_GAP_PX) + TOP_MARGIN_PX;
        var min = Math.min(0, _viewPortHeight - (contentHeight + PAGE_PADDING_PX + BOTTOM_MARGIN_PX));
        _scroll.setBounds(min, 0);
    }

    public void resetScroll() {
        _scroll.setScrollOffset(0);
    }

    @Override
    public boolean isCatchingGestures() {
        return _contextMenu != null && _contextMenu.isOpened();
    }

    @Override
    public void dispose() {
        _items.forEach(ListItem::dispose);
        if (_contextMenu != null) {
            _contextMenu.dispose();
        }
    }
}
