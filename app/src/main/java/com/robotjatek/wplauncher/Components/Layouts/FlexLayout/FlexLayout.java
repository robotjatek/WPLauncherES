package com.robotjatek.wplauncher.Components.Layouts.FlexLayout;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Layouts.LayoutInfo;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: layout padding
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
    private Size<Integer> _size = new Size<>(-1, -1);
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
        _itemDrawContext = new FlexLayoutItemDrawContext(this);
    }

    public void addChild(UIElement element) {
        _children.add(element);
        _dirty = true;
    }

    private void layout() {
        switch (_direction) {
            case ROW -> layoutRow();
            case COLUMN -> layoutColumn();
        }
    }

    private void layoutRow() {
        _layoutInfo.clear();
        var totalWidth = 0f;
        for (var child : _children) {
            var size = child.measure();
            totalWidth += size.width();
        }

        // horizontal alignment based on justify
        var childX = switch (_justify.justify()) {
            case START -> 0;
            case END -> _size.width() - totalWidth;
            case CENTER -> {
                var offset = (_size.width() - totalWidth) / 2;
                yield _justify.safe ? Math.max(0, offset) : offset;
            }
        };

        // vertical alignment based on align + store layout info
        for (var child : _children) {
            var size = child.measure();
            var childY = switch (_align) {
                case START, STRETCH -> 0f;
                case END -> _size.height() - size.height();
                case CENTER -> (_size.height() - size.height()) / 2;
            };

            _layoutInfo.put(child, new LayoutInfo(childX, childY));

            if (_align == AlignItems.STRETCH) {
                throw new UnsupportedOperationException("Implement UI element resize");
            }

            childX += size.width();
        }
    }
    private void layoutColumn() {
        _layoutInfo.clear();

        // measure total height of items
        var totalHeight = 0f;
        for (var child : _children) {
            // TODO: handle if the child is a layout itself
            var size = child.measure();
            totalHeight += size.height();
        }

        // vertical alignment based on justify
        var childY = switch (_justify.justify()) {
            case START -> 0f;
            case END -> _size.height() - totalHeight;
            case CENTER -> {
                var offset = (_size.height() - totalHeight) / 2;
                yield _justify.safe() ? Math.max(0, offset) : offset;
            }
        };
        // horizontal alignment + store layout info
        for (var child : _children) {
            var size = child.measure();
            var childX = switch (_align) {
                case START -> 0;
                case END -> _size.width() - size.width();
                case CENTER -> (_size.width() - size.width()) / 2;
                case STRETCH -> throw new UnsupportedOperationException("Implement UI element resize first");
            };

            _layoutInfo.put(child, new LayoutInfo(childX, childY));
            childY += size.height();
        }
    }

    @Override
    public IDrawContext<UIElement> getContext() {
        return _itemDrawContext;
    }

    @Override
    public void onResize(int width, int height) {
        _size = new Size<>(width, height);
        _dirty = true;
    }

    @Override
    public int getWidth() {
        return _size.width();
    }

    @Override
    public int getHeight() {
        return _size.height();
    }

    @Override
    public LayoutInfo getLayoutInfo(UIElement item) {
        return _layoutInfo.get(item);
    }

    @Override
    public void draw(float delta, float[] proj, float[] viewMatrix, QuadRenderer renderer,
                     Position<Float> position, Size<Integer> size) {
        if (!_size.equals(size)) {
            _size = size;
            _dirty = true;
        }
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
