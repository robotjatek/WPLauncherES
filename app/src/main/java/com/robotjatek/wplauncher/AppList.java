package com.robotjatek.wplauncher;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO: icon
// TODO: onclick handler?
class ListItem {
    private final String _label;
    private int _textureId;

    public ListItem(String label, int width, int height) {
        _label = label;
        _textureId = TileUtil.createTextTexture(label, width, height, 0xffffffff);
    }

    public String getLabel() {
        return _label;
    }

    public int getTextureId() {
        return _textureId;
    }

    public void dispose() {
        TileUtil.deleteTexture(_textureId);
        _textureId = -1;
    }
}

// TODO: a scrollingot kiszervezni egy külön (base?)osztályba -- manual scroll.onTouch* calls are error prone
// TODO: meg a view alapú render logicot is...
public class AppList implements Page {

    private final float[] scrollMatrix = new float[16]; // scroll position transformation
    private final float[] modelMatrix = new float[16];

    // TODO: List item renderer
    private final QuadRenderer testRenderer = new QuadRenderer(); // TODO: this is just to show some test data as content
    private final ScrollController _scroll = new ScrollController();

    private List<ListItem> _items = new ArrayList<>();

    private static final int TOP_MARGIN_PX = 152;
    private static final int ITEM_HEIGHT_PX = 128;
    private static final int ITEM_GAP_PX = 5;
    private static final int PAGE_PADDING_PX = 24;
    private int _listWidth;

    public AppList() {}

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        _scroll.update(delta);

        Matrix.setIdentityM(scrollMatrix, 0);
        Matrix.translateM(scrollMatrix, 0, 0, _scroll.getScrollOffset() + TOP_MARGIN_PX, 0);

        for (var i = 0; i < _items.size(); i++) {
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, PAGE_PADDING_PX, i * (ITEM_HEIGHT_PX + ITEM_GAP_PX), 0);
            Matrix.scaleM(modelMatrix, 0, _listWidth - PAGE_PADDING_PX, ITEM_HEIGHT_PX, 0);

            Matrix.multiplyMM(modelMatrix, 0, scrollMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelMatrix, 0, viewMatrix, 0, modelMatrix, 0);

            testRenderer.draw(projMatrix, modelMatrix, _items.get(i).getTextureId());
        }
    }

    @Override
    public void touchMove(float x, float y) {
        _scroll.onTouchMove(y);
    }

    @Override
    public void touchStart(float x, float y) {
        _scroll.onTouchStart(y);
    }

    @Override
    public void touchEnd(float x, float y) {
        _scroll.onTouchEnd();
    }

    @Override
    public void handleLongPress(float x, float y) {
        // TODO: handle long press
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
        return labels.stream().map(l -> new ListItem(l, width, ITEM_HEIGHT_PX))
                .collect(Collectors.toList());
    }

    @Override
    public void dispose() {
        _items.forEach(ListItem::dispose);
        _items.clear();
        testRenderer.dispose();
    }
}
