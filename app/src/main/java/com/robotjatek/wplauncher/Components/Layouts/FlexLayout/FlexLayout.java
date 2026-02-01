package com.robotjatek.wplauncher.Components.Layouts.FlexLayout;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Layouts.LayoutInfo;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FlexLayoutItemDrawContext implements IDrawContext<UIElement> {

    private final ILayout _layout;

    public FlexLayoutItemDrawContext(FlexLayout layout) {
        _layout = layout;
    }

    @Override
    public float xOf(UIElement element) {
        return _layout.getLayoutInfo(element).x();
    }

    @Override
    public float yOf(UIElement element) {
        return _layout.getLayoutInfo(element).y();
    }

    @Override
    public float widthOf(UIElement element) {
        return _layout.getWidth();
    }

    @Override
    public float heightOf(UIElement element) {
        return element.measure().height();
    }
}

// TODO: direction row/column
// TODO: justify content: vertical align if column, horizontal if row
// TODO: align items: vertical if row, horizontal if column
// TODO: column direction only for POC
// TODO: row direction implementation after column
// TODO: allow to put other layouts as children
// TODO: bg color for a layout
/**
 * A minimal flex layout implementation
 */
public class FlexLayout implements ILayout {
    public enum Direction {
        ROW,
        COLUMN
    }

    public enum JustifyContentEnum {
        START,
        END,
        CENTER
    }

    /**
     * Controls the alignment of all items on the main axis (vertical align if column, horizontal if row)
     * @param justify
     * @param safe Only overflow to right if centered, keeping the left side inside the container)
     */
    public record JustifyContent(JustifyContentEnum justify, boolean safe) {}

    /**
     * Controls the alignment of items on the cross axis
     * (horizontal alignment if column, vertical if row)
     */
    public enum AlignItems {
        START,
        END,
        CENTER,
        STRETCH
    }

    private final List<UIElement> _children = new ArrayList<>();
    private final Map<UIElement, LayoutInfo> _layoutInfo = new HashMap<>();
    private int _width;
    private int _height;
    private final float[] _modelMatrix = new float[16];
    private final JustifyContent _justify;
    private final AlignItems _align;
    private final Direction _direction;
    private final IDrawContext<UIElement> _itemDrawContext;
    private boolean _dirty = true;

    public FlexLayout(
            JustifyContent justify,
            AlignItems align,
            Direction direction) {
        _justify = justify;
        _align = align;
        _direction = direction;
        // TODO: create proper drawcontext
        _itemDrawContext = new FlexLayoutItemDrawContext(this);
    }

    public void addChild(UIElement element) {
        _children.add(element);
        _dirty = true;
    }

    private void layout() {
        // TODO: separate layout col and row methods
        switch (_direction) {
            case ROW -> throw new UnsupportedOperationException("Implement me :(");
            case COLUMN -> layoutColumn();
        }
    }

    private void layoutColumn() {
        // TODO: implement
        _layoutInfo.clear();
        var totalHeight = 0;
        for (var child : _children) {
            var size = child.measure();
            // TODO: handle vertical alignment
            // TODO: handle horizontal alignment
            // TODO: handle if the child is a layout itself
            _layoutInfo.put(child, new LayoutInfo(0, totalHeight));
            totalHeight += size.height();
        }
        _height = totalHeight;
    }

    @Override
    public IDrawContext<UIElement> getContext() {
        return _itemDrawContext; // TODO: drawcontext for children
    }

    @Override
    public void onResize(int width, int height) {
        // TODO: available w/h?
        _width = width;
        _height = height;
        _dirty = true;
    }

    @Override
    public int getWidth() {
        return _width;
    }

    @Override
    public int getHeight() {
        return _height;
    }

    @Override
    public LayoutInfo getLayoutInfo(UIElement item) {
        return _layoutInfo.get(item);
    }

    @Override
    public void draw(float delta, float[] proj, float[] viewMatrix, QuadRenderer renderer, Position<Float> position) {
        if (_dirty) {
            layout();
            _dirty = false;
        }
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, position.x(), position.y(), 0f);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        for (var child : _children) {
            child.draw(proj, _modelMatrix, this, renderer);
        }
    }

    @Override
    public void onTouchStart(float x, float y) {

    }

    @Override
    public void onTouchEnd(float x, float y) {

    }

    @Override
    public void onTouchMove(float x, float y) {

    }

    @Override
    public void dispose() {
        _children.forEach(UIElement::dispose);
        _children.clear();
    }
}
