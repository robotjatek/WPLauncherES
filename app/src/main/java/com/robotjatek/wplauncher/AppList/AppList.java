package com.robotjatek.wplauncher.AppList;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.ScrollController;
import com.robotjatek.wplauncher.Shader;
import com.robotjatek.wplauncher.TileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class MenuOption {

    private final String _label;
    private final Runnable _action;
    private final int _width;
    private final int _height;

    private final int _textureId;

    public MenuOption(String label, Runnable action, int width, int height) {
        _label = label;
        _action = action;
        _width = width;
        _height = height;
        _textureId = TileUtil.createTextTexture(label, width, height, 0xffffffff); // TODO: más text alignmentet is supportálni
    }

    public void onTap() {
        if (_action != null) {
            _action.run();
        }
    }
}

class ContextMenu {

    List<MenuOption> _options = new ArrayList<>();

    public ContextMenu(List<MenuOption> options, float x, float y) {

    }

    public void draw(float delta, float[] proj, float[] view) {

        // TODO: model matrix

        // TODO: draw bg
        // TODO: draw each option -> transparent bg, white textColor
    }

}

// TODO: a scrollingot kiszervezni egy külön (base?)osztályba -- manual scroll.onTouch* calls are error prone
// TODO: meg a view alapú render logicot is...
public class AppList implements Page, ListItemDrawContext {

    private final float[] scrollMatrix = new float[16]; // scroll position transformation
    private final float[] modelMatrix = new float[16];

    // TODO: List item renderer?
    private final Shader _shader = new Shader("", "");
    private final QuadRenderer testRenderer = new QuadRenderer(_shader); // TODO: this is just to show some test data as content
    private final ScrollController _scroll = new ScrollController();

    private List<ListItem> _items = new ArrayList<>();

    private static final int TOP_MARGIN_PX = 152;
    private static final int ITEM_HEIGHT_PX = 128;
    private static final int ITEM_GAP_PX = 5;
    private static final int PAGE_PADDING_PX = 24;
    private int _listWidth;
    private boolean _isTouching = false;

    private ContextMenu _contextMenu;

    public AppList() {}

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        _scroll.update(delta);

        Matrix.setIdentityM(scrollMatrix, 0);
        Matrix.translateM(scrollMatrix, 0, 0, _scroll.getScrollOffset() + TOP_MARGIN_PX, 0);
        Matrix.multiplyMM(scrollMatrix, 0, scrollMatrix, 0, viewMatrix, 0);

        if (_contextMenu != null) {
            _contextMenu.draw(delta, projMatrix, viewMatrix);
        }

        for (var i = 0; i < _items.size(); i++) {
            var item = _items.get(i);
            item.update(delta);
            item.draw(delta, projMatrix, scrollMatrix);
        }
    }

    @Override
    public void touchMove(float x, float y) {
        _scroll.onTouchMove(y);
    }

    @Override
    public void touchStart(float x, float y) {
        _scroll.onTouchStart(y);
        _isTouching = true;
    }

    @Override
    public void touchEnd(float x, float y) {
        _scroll.onTouchEnd();

        if (_isTouching) {
            handleTap(x, y);
            _isTouching = false;
        }
    }

    @Override
    public void handleLongPress(float x, float y) {
        var tappedItem = getItemAt(y);
        tappedItem.ifPresent(i -> i.setLabel("Long press"));
    }

    @Override
    public void onSizeChanged(int width, int height) {
        _listWidth = width - 2 * PAGE_PADDING_PX;
        _items.forEach(ListItem::dispose);
        var labels = List.of("Első", "Második", "Harmadik", "Negyedik", "Ötödik", "Hatodik",
                "Első", "Második", "Harmadik", "Negyedik", "Ötödik", "Hatodik",
                "Első", "Második", "Harmadik", "Negyedik", "Ötödik", "Hatodik",
                "Első", "Második", "Harmadik", "Negyedik", "Ötödik", "Hatodik"); // TODO: remove later
        _items = createItems(labels, _listWidth);

        var contentHeight = _items.size() * (ITEM_HEIGHT_PX + ITEM_GAP_PX);

        // content fits on screen, don't allow scrolling
        if (contentHeight <= height) {
            _scroll.setBounds(0, 0);
        } else {
            var minScroll = -(contentHeight - height) - TOP_MARGIN_PX;
            _scroll.setBounds(minScroll, 0);
        }
    }

    private List<ListItem> createItems(List<String> labels, int width) {
        return labels.stream().map(l -> new ListItem(l, width, ITEM_HEIGHT_PX, this))
                .collect(Collectors.toList());
    }

    private void handleTap(float x, float y) {
        var tappedItem = getItemAt(y);
        tappedItem.ifPresent(i -> i.setLabel("Tapped")); // TODO: handle tap, start app
    }

    private Optional<ListItem> getItemAt(float y) {
        var adjustedY = y - (_scroll.getScrollOffset() + TOP_MARGIN_PX);
        var index = (int)(adjustedY / (ITEM_HEIGHT_PX + ITEM_GAP_PX));
        if (index >= 0 && index < _items.size()) {
            return Optional.of(_items.get(index));
        }

        return Optional.empty();
    }

    @Override
    public QuadRenderer getRenderer() {
        return testRenderer;
    }

    @Override
    public float x(ListItem item) {
        return PAGE_PADDING_PX;
    }

    @Override
    public float y(ListItem item) {
        var index = _items.indexOf(item);
        if (index == -1) {
            throw new RuntimeException("List item not found");
        }
        return index * (ITEM_HEIGHT_PX + ITEM_GAP_PX);
    }

    @Override
    public float width(ListItem item) {
        return _listWidth - PAGE_PADDING_PX;
    }

    @Override
    public float height(ListItem item) {
        return ITEM_HEIGHT_PX;
    }

    @Override
    public void dispose() {
        _items.forEach(ListItem::dispose);
        _items.clear();
        _shader.delete();
    }
}
