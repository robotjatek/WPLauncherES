package com.robotjatek.wplauncher.Components.ListView;

import android.opengl.Matrix;
import android.util.Log;

import com.robotjatek.wplauncher.AppList.IItemListContainer;
import com.robotjatek.wplauncher.Components.ListPage.ListItem;
import com.robotjatek.wplauncher.Components.ListPage.ListItemDrawContext;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.QuadRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list that is embeddable to any layouts
 * @param <T> The type of the payload
 */
public class ListView<T> implements UIElement, IItemListContainer<T> {
    public static final int ITEM_HEIGHT_PX = 128;
    public static final int ITEM_GAP_PX = 5;
    private boolean _disposed = false;
    private boolean _dirty = true;
    private final float[] _modelMatrix = new float[16];
    private final List<ListItem<T>> _items = Collections.synchronizedList(new ArrayList<>());
    private Size<Integer> _size = new Size<>(-1, -1);
    private IState _state; // TODO: create states
    private final ListItemDrawContext<T, ListView<T>> _itemDrawContext = new ListItemDrawContext<>(0, ITEM_HEIGHT_PX, ITEM_GAP_PX, this); // TODO: remove hardcoded values

    public ListView() {

    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
      // _state.update(delta); // TODO: state machine

        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_dirty) {
            _itemDrawContext.onResize(w);
            _items.forEach(ListItem::setDirty);
            _dirty = false;
        }

        // TODO: scroll support
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);

        for (var item : _items) {
            item.update(_itemDrawContext);
            item.draw(delta, proj, _modelMatrix, _itemDrawContext, renderer);
        }
    }

    public void setSize(Size<Integer> size) {
        // TODO: in the future only draw elements up until the size
        _size = size;
        //_contextMenuDrawContext.onResize(_listWidth, height); // TODO
        _dirty = true;
    }

    @Override
    public List<ListItem<T>> getItems() {
        return _items;
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public void addItems(List<ListItem<T>> items) {
        _items.addAll(items);
        _items.forEach(ListItem::setDirty);
        // TODO: setscrollbounds
    }

    public void removeItem(ListItem<T> item) {
        _items.remove(item);
        item.dispose();
        // TODO: setScrollBounds
    }

    @Override
    public Size<Integer> measure() {
        var contentHeight = _items.size() * (ITEM_HEIGHT_PX + ITEM_GAP_PX);
        return new Size<>(_size.width(), contentHeight);
    }

    @Override
    public void onTap() {
        // TODO: identify the tapped item -> signal tap to the item
        // TODO: new gesture logic?
        // Triggerelődik az ontap most is -> viszont move endnél is... kelleni fog az a new gesture...
        Log.d("","");
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _items.forEach(ListItem::dispose);
            _disposed = true;
        }
    }
}
