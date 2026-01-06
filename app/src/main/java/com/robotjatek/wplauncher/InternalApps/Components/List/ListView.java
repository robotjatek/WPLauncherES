package com.robotjatek.wplauncher.InternalApps.Components.List;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.IItemListContainer;
import com.robotjatek.wplauncher.AppList.ListItem;
import com.robotjatek.wplauncher.AppList.ListItemDrawContext;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.InternalApps.Components.List.States.IdleState;
import com.robotjatek.wplauncher.InternalApps.Components.List.States.ScrollState;
import com.robotjatek.wplauncher.InternalApps.Components.List.States.TappedState;
import com.robotjatek.wplauncher.InternalApps.Components.List.States.TouchingState;
import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.ScrollController;

import java.util.ArrayList;
import java.util.List;

// TODO: ezt az applistben is használni
// TODO: handle tap
// TODO: longpress, hogy az applistben is működjön
// TODO: ListItemDrawContext és ListItem ezzel kerüljön egy mappába
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

    private IState _state = IDLE_STATE();
    public static final int TOP_MARGIN_PX = 0;
    public static final int ITEM_HEIGHT_PX = 128;
    public static final int ITEM_GAP_PX = 5;
    private static final int PAGE_PADDING_PX = 60;
    public static final float BOTTOM_MARGIN_PX = 192;
    private final float[] scrollMatrix = new float[16];
    private final ScrollController _scroll = new ScrollController();
    private final ListItemDrawContext<T, ListView<T>> _drawContext;
    private final List<ListItem<T>> _items = new ArrayList<>();
    private int _viewPortHeight;

    public ListView(QuadRenderer renderer) {
        _drawContext = new ListItemDrawContext<>(PAGE_PADDING_PX, ITEM_HEIGHT_PX, ITEM_GAP_PX, this, renderer);
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
        _items.forEach(ListItem::setDirty);
        setScrollBounds();
    }

    public ListItemDrawContext<T, ListView<T>> getDrawContext() {
        return _drawContext;
    }

    public void addItems(List<ListItem<T>> items) {
        _items.addAll(items);
        _items.forEach(ListItem::setDirty);
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public ScrollController getScroll() {
        return _scroll;
    }

    private void setScrollBounds() {
        var contentHeight = _items.size() * (ITEM_HEIGHT_PX + ITEM_GAP_PX) + TOP_MARGIN_PX;
        var min = Math.min(0, _viewPortHeight - (contentHeight + PAGE_PADDING_PX + BOTTOM_MARGIN_PX));
        _scroll.setBounds(min, 0);
    }

    @Override
    public void dispose() {
        _items.forEach(ListItem::dispose);
    }
}
