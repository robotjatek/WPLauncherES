package com.robotjatek.wplauncher.Components.ListView;

import android.opengl.GLES32;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.IItemListContainer;
import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.Components.ContextMenu.IContextMenuParent;
import com.robotjatek.wplauncher.Components.ListView.States.ContextMenuState;
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
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A list that is embeddable to any layouts, with arbitrary size (if the said layout supports it)
 * @param <T> The type of the payload
 */
public class ListView<T> implements UIElement, IItemListContainer<T>, IContextMenuParent {
    public static final int ITEM_HEIGHT_PX = 128;
    public static final int ITEM_GAP_PX = 5;
    private boolean _disposed = false;
    private boolean _dirty = true;
    private final float[] _modelMatrix = new float[16];
    private final float[] _menuMatrix = new float[16];
    private final List<ListItem<T>> _allItems = Collections.synchronizedList(new ArrayList<>());
    private final List<ListItem<T>> _visibleItems = Collections.synchronizedList(new ArrayList<>());
    private Size<Integer> _size = new Size<>(-1, -1);
    private final ScrollController _scroll = new ScrollController();
    private IState _state = IDLE_STATE();
    private final ListItemDrawContext<T, ListView<T>> _itemDrawContext;
    private final Queue<Runnable> _commands = new ConcurrentLinkedQueue<>();
    private final int _padding;
    private int _topMargin;
    private int _bottomMargin;
    private ContextMenu<T> _contextMenu;

    public IState IDLE_STATE() {
        return new IdleState<>(this);
    }

    public IState SCROLL_STATE(float y) {
        return new ScrollState<>(this, y);
    }

    public IState CONTEXT_MENU_STATE(float x, float y) {
        return new ContextMenuState<>(this, x, y);
    }

    public ListView(int topMargin, int bottomMargin, int padding) {
        _topMargin = topMargin;
        _bottomMargin = bottomMargin;
        _padding = padding;
        _itemDrawContext = new ListItemDrawContext<>(padding, ITEM_HEIGHT_PX, ITEM_GAP_PX, this);
    }

    public int getPadding() {
        return _padding;
    }

    public void filter(String filter) {
        _commands.add(() -> {
            _visibleItems.clear();
            var filtered = _allItems.stream().filter(i -> i.getLabel().toLowerCase().contains(filter)).toList();
            _visibleItems.addAll(filtered);
            setScrollBounds();
            _dirty = true;
        });
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
            _allItems.forEach(ListItem::setDirty);
            _dirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y + _scroll.getScrollOffset() + _padding, 0);
        Matrix.multiplyMM(_modelMatrix, 0, _modelMatrix, 0, view, 0);

        var screenHeight = LauncherRenderer.SCREEN_DATA.screenHeight;
        var glY = screenHeight - _topMargin - y - h;
        GLES32.glEnable(GLES32.GL_SCISSOR_TEST);
        GLES32.glScissor((int) x, (int) glY, w, h);
        for (var item : _visibleItems) {
            item.update(_itemDrawContext);
            item.draw(delta, proj, _modelMatrix, _itemDrawContext, renderer); // TODO: do not call draw() for elements that are scrolled out of the view
        }
        GLES32.glDisable(GLES32.GL_SCISSOR_TEST);

        // Draw the context menu last so it shows up above everything else
        if (_contextMenu != null && _contextMenu.isOpened()) {
            Matrix.setIdentityM(_menuMatrix, 0);
            Matrix.translateM(_menuMatrix, 0, x, y, 0);
            Matrix.multiplyMM(_menuMatrix, 0, view, 0, _menuMatrix, 0);
            _contextMenu.draw(delta, proj, _menuMatrix, renderer);
        }
    }

    public void setSize(Size<Integer> size) {
        _size = size;
        _itemDrawContext.onResize(size.width());
        setScrollBounds();
        _dirty = true;
    }

    @Override
    public Size<Integer> getSize() {
        return _size;
    }

    private void setScrollBounds() {
        var contentHeight = _visibleItems.size() * (ITEM_HEIGHT_PX + ITEM_GAP_PX) + _bottomMargin;
        var min = Math.min(0, _size.height() - (contentHeight + _topMargin + _bottomMargin));
        _scroll.setBounds(min, 0);
    }

    @Override
    public List<ListItem<T>> getItems() {
        return _allItems;
    }

    @Override
    public List<ListItem<T>> getVisibleItems() {
        return _visibleItems;
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public void addItem(int index, ListItem<T> item) {
        _commands.add(() -> {
            _allItems.add(index, item);
            item.setDirty();
            setScrollBounds();
        });
    }

    public void addItems(List<ListItem<T>> items) {
        _commands.add(() -> {
            _allItems.addAll(items);
            _allItems.forEach(ListItem::setDirty);
            _visibleItems.addAll(items);
            setScrollBounds();
        });
    }

    public void removeItemByPayload(T payload) {
        _commands.add(() -> {
            var item = _allItems.stream().filter(i -> i.getPayload().equals(payload)).findFirst();
            item.ifPresent(i -> {
                _allItems.remove(i);
                _visibleItems.remove(i);
                i.dispose();
            });
            setScrollBounds();
        });
    }

    @Override
    public Size<Integer> measure() {
        // explicitly sized
        if (_size.width() != -1 && _size.height() != -1) {
            return _size;
        }

        // No size was given: measure
        // Items are fixed sized as of now
        var height = (ITEM_HEIGHT_PX + ITEM_GAP_PX) * _allItems.size();
        var width = -1; // TODO: how to measure width? ask the parent? measure the elements?
        _size = new Size<>(width, height);
        setScrollBounds();
        return _size;
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
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

    public ContextMenu<T> openContextMenu(float x, float y, T item) {
        if (_contextMenu != null) {
            _contextMenu.open(new Position<>(x, y), item);
        }
        return _contextMenu;
    }

    public void closeContextMenu() {
        if (_contextMenu != null) {
            _contextMenu.close();
        }
    }

    public boolean isCatchingGestures() {
        return _state.isCatchingGestures();
    }

    public ScrollController getScroll() {
        return _scroll;
    }

    public void resetState() {
        if (!(_state instanceof IdleState<?>)) {
            changeState(IDLE_STATE());
        }
    }

    public void setMargins(int topMargin, int bottomMargin) {
        if (_topMargin != topMargin || _bottomMargin != bottomMargin) {
            _topMargin = topMargin;
            _bottomMargin = bottomMargin;
            setScrollBounds();
            _dirty = true;
        }
    }

    @Override
    public int getTopMargin() {
        return _topMargin;
    }

    @Override
    public int getBottomMargin() {
        return _bottomMargin;
    }

    public IDrawContext<ListItem<T>> getItemDrawContext() {
        return _itemDrawContext;
    }

    private void executeCommands() {
        Runnable command;
        while ((command = _commands.poll()) != null) {
            command.run();
        }
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _allItems.forEach(ListItem::dispose);
            _disposed = true;
        }
    }
}
