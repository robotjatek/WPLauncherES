package com.robotjatek.wplauncher.Components.ListView;

import android.opengl.GLES32;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.IItemListContainer;
import com.robotjatek.wplauncher.Components.ListPage.ListItem;
import com.robotjatek.wplauncher.Components.ListPage.ListItemDrawContext;
import com.robotjatek.wplauncher.Components.ListView.States.IdleState;
import com.robotjatek.wplauncher.Components.ListView.States.ScrollState;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.ScrollController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A list that is embeddable to any layouts, with arbitrary size (if the said layout supports it)
 * @param <T> The type of the payload
 */
public class ListView<T> implements UIElement, IItemListContainer<T> {
    public static final int ITEM_HEIGHT_PX = 128;
    public static final int ITEM_GAP_PX = 5;
    public static final int PADDING_PX = 0;
    private boolean _disposed = false;
    private boolean _dirty = true;
    private final float[] _modelMatrix = new float[16];
    private final List<ListItem<T>> _items = Collections.synchronizedList(new ArrayList<>());
    private Size<Integer> _size = new Size<>(-1, -1);
    private final ScrollController _scroll = new ScrollController();
    private IState _state = IDLE_STATE();
    private final ListItemDrawContext<T, ListView<T>> _itemDrawContext = new ListItemDrawContext<>(PADDING_PX, ITEM_HEIGHT_PX, ITEM_GAP_PX, this); // TODO: remove hardcoded values
    private final Queue<Runnable> _commands = new ConcurrentLinkedQueue<>();
    private final int _topMargin;
    private final int _bottomMargin;

    public IState IDLE_STATE() {
        return new IdleState<>(this);
    }

    public IState SCROLL_STATE(float y) {
        return new ScrollState<>(this, y);
    }

    public ListView(int topMargin, int bottomMargin) {
        _topMargin = topMargin;
        _bottomMargin = bottomMargin;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        executeCommands();
        _state.update(delta);

        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_dirty) {
            _itemDrawContext.onResize(w);
            _items.forEach(ListItem::setDirty);
            _dirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y + _scroll.getScrollOffset(), 0);
        Matrix.multiplyMM(_modelMatrix, 0, _modelMatrix, 0, view, 0);

        var screenHeight = LauncherRenderer.SCREEN_DATA.screenHeight;
        var glY = screenHeight - y - h - _bottomMargin;
        GLES32.glEnable(GLES32.GL_SCISSOR_TEST);
        GLES32.glScissor((int) x, (int) glY, w, h);
        for (var item : _items) {
            item.update(_itemDrawContext);
            item.draw(delta, proj, _modelMatrix, _itemDrawContext, renderer); // TODO: do not call draw() for elements that are scrolled out of the view
        }
        GLES32.glDisable(GLES32.GL_SCISSOR_TEST);

        // TODO: draw context menu
    }

    public void setSize(Size<Integer> size) {
        _size = size;
        _itemDrawContext.onResize(size.width());
        setScrollBounds();
        //_contextMenuDrawContext.onResize(_listWidth, height); // TODO
        _dirty = true;
    }

    private void setScrollBounds() {
        var contentHeight = _items.size() * (ITEM_HEIGHT_PX + ITEM_GAP_PX) + _bottomMargin;
        var min = Math.min(0, _size.height() - (contentHeight + PADDING_PX));
        _scroll.setBounds(min, 0);
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
        setScrollBounds();
    }

    public void removeItemByPayload(T payload) {
        _commands.add(() -> {
            var item = _items.stream().filter(i -> i.getPayload().equals(payload)).findFirst();
            item.ifPresent(i -> {
                _items.remove(i);
                i.dispose();
            });
            setScrollBounds();
        });
    }

    private void executeCommands() {
        Runnable command;
        while ((command = _commands.poll()) != null) {
            command.run();
        }
    }

    @Override
    public Size<Integer> measure() {
        // explicitly sized
        if (_size.width() != -1 && _size.height() != -1) {
            return _size;
        }

        // No size was given: measure
        // Items are fixed sized as of now
        var height = (ITEM_HEIGHT_PX + ITEM_GAP_PX) * _items.size();
        var width = -1; // TODO: how to measure width? ask the parent? measure the elements?
        _size = new Size<>(width, height);
        setScrollBounds();
        return _size;
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
    }

    public ScrollController getScroll() {
        return _scroll;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _items.forEach(ListItem::dispose);
            _disposed = true;
        }
    }
}
