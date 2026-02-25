package com.robotjatek.wplauncher.Components.Layouts.FlexLayout;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Layouts.LayoutInfo;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: layout padding
/**
 * A minimal flex layout implementation
 */
public class FlexLayout implements ILayout, UIElement {
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
     * @param safe Only overflow to right if centered, keeping the left side inside the container
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
    private float _flexGrow = 0f;
    private final IDrawContext<UIElement> _itemDrawContext;
    private boolean _dirty = true;
    private int _bgColor = Colors.TRANSPARENT;
    private int _bgTexture = -1;

    public FlexLayout(
            JustifyContent justify,
            AlignItems align,
            Direction direction) {
        _justify = justify;
        _align = align;
        _direction = direction;
        _itemDrawContext = new FlexLayoutItemDrawContext(this);
    }

    public AlignItems getAlign() {
        return _align;
    }

    public Direction getDirection() {
        return _direction;
    }

    public void setFlexGrow(float grow) {
        _flexGrow = grow;
        _dirty = true;
    }

    public float getFlexGrow() {
        return _flexGrow;
    }

    public void addChild(UIElement element) {
        _children.add(element);
        _dirty = true;
    }

    public void setBgColor(int bgColor) {
        _bgColor = bgColor;
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

        // First pass: measure fixed-size children and calculate total flex-grow
        float totalWidth = 0f;
        float totalFlexGrow = 0f;

        for (var child : _children) {
            if (child instanceof FlexLayout childLayout && childLayout.getFlexGrow() > 0) {
                totalFlexGrow += childLayout.getFlexGrow();
            } else {
                var size = child.measure();
                totalWidth += size.width();
            }
        }

        // Calculate remaining space for flex items
        float remainingSpace = Math.max(0, _size.width() - totalWidth);

        // horizontal alignment based on justify (only applies if no flex-grow)
        var childX = 0f;
        if (totalFlexGrow == 0) {
            childX = switch (_justify.justify()) {
                case START -> 0f;
                case END -> _size.width() - totalWidth;
                case CENTER -> {
                    var offset = (_size.width() - totalWidth) / 2f;
                    yield _justify.safe ? Math.max(0, offset) : offset;
                }
            };
        }

        // Second pass: layout children
        for (var child : _children) {
            float childWidth;

            // Check if this child should grow
            if (child instanceof FlexLayout childLayout && childLayout.getFlexGrow() > 0) {
                // This child gets a portion of remaining space
                childWidth = (remainingSpace / totalFlexGrow) * childLayout.getFlexGrow();
                // Resize the child layout
                var naturalSize = childLayout.measure();
                childLayout.onResize(
                        (int) childWidth,
                        _align == AlignItems.STRETCH ? _size.height() : naturalSize.height().intValue()
                );
            } else {
                var size = child.measure();
                childWidth = size.width();
            }

            var childHeight = child.measure().height();
            var childY = switch (_align) {
                case START, STRETCH -> 0f;
                case END -> _size.height() - childHeight;
                case CENTER -> (_size.height() - childHeight) / 2f;
            };

            _layoutInfo.put(child, new LayoutInfo(childX, childY));
            childX += childWidth;
        }
    }

    private void layoutColumn() {
        _layoutInfo.clear();

        // First pass: measure fixed-size children and calculate total flex-grow
        float totalHeight = 0f;
        float totalFlexGrow = 0f;

        for (var child : _children) {
            if (child instanceof FlexLayout childLayout && childLayout.getFlexGrow() > 0) {
                totalFlexGrow += childLayout.getFlexGrow();
            } else {
                var size = child.measure();
                totalHeight += size.height();
            }
        }

        // Calculate remaining space for flex items
        float remainingSpace = Math.max(0, _size.height() - totalHeight);

        // vertical alignment based on justify (only applies if no flex-grow)
        var childY = 0f;
        if (totalFlexGrow == 0) {
            childY = switch (_justify.justify()) {
                case START -> 0f;
                case END -> _size.height() - totalHeight;
                case CENTER -> {
                    var offset = (_size.height() - totalHeight) / 2f;
                    yield _justify.safe() ? Math.max(0, offset) : offset;
                }
            };
        }

        // Second pass: layout children
        for (var child : _children) {
            float childHeight;

            // Check if this child should grow
            if (child instanceof FlexLayout childLayout && childLayout.getFlexGrow() > 0) {
                // This child gets a portion of remaining space
                childHeight = (remainingSpace / totalFlexGrow) * childLayout.getFlexGrow();
                // Resize the child layout
                var naturalSize = childLayout.measure();
                childLayout.onResize(
                        _align == AlignItems.STRETCH ? _size.width() : naturalSize.width().intValue(),
                        (int) childHeight
                );
            } else {
                var size = child.measure();
                childHeight = size.height();
            }

            var childWidth = child.measure().width();
            var childX = switch (_align) {
                case START, STRETCH -> 0;
                case END -> _size.width() - childWidth;
                case CENTER -> (_size.width() - childWidth) / 2;
            };

            _layoutInfo.put(child, new LayoutInfo(childX, childY));
            childY += childHeight;
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
        // TODO: dont pass the pos and size directly query it from the parent
        //  - statictilecontent or an other layout (flexlayoutDrawcontext)

        if (!_size.equals(size)) {
            _size = size;
            _dirty = true;
        }
        if (_dirty) {
            layout();
            TileUtil.deleteTexture(_bgTexture);
            _bgTexture = BitmapUtil.createTextureFromBitmap(BitmapUtil.createRect(1, 1, 0, _bgColor));
            _dirty = false;
        }

        drawBg(position, size, proj, viewMatrix, renderer);
        drawChildren(proj, viewMatrix, renderer, position);
    }

    private void drawBg(Position<Float> position, Size<Integer> size, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer) {
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, position.x(), position.y(), 0f);
        Matrix.scaleM(_modelMatrix, 0, size.width(), size.height(), 1);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        renderer.draw(projMatrix, _modelMatrix, _bgTexture);
    }

    private void drawChildren(float[] proj, float[] viewMatrix, QuadRenderer renderer, Position<Float> position) {
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, position.x(), position.y(), 0f);
        Matrix.multiplyMM(_modelMatrix, 0, viewMatrix, 0, _modelMatrix, 0);
        for (var child : _children) {
            child.draw(proj, _modelMatrix, _itemDrawContext, renderer);
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
    public void draw(float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        // when drawn as a child
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var width = (int) drawContext.widthOf(this);
        var height = (int) drawContext.heightOf(this);

        // call the ILayout draw method
        draw(0, proj, view, renderer, new Position<>(x, y), new Size<>(width, height));
    }

    @Override
    public Size<Integer> measure() {
        // If we've been explicitly sized, return that
        if (_size.width() != -1 && _size.height() != -1) {
            var s = new Size<>(_size.width(), _size.height());
            _size = s;
            return s;
        }

        // Otherwise, calculate intrinsic size based on children and direction
        if (_children.isEmpty()) {
            _size = new Size<>(0, 0);
            return _size;
        }

        if (_direction == Direction.COLUMN) {
            var totalHeight = 0;
            var maxWidth = 0;
            for (var child : _children) {
                var childSize = child.measure();
                totalHeight += childSize.height();
                maxWidth = Math.max(maxWidth, childSize.width());
            }
            _size = new Size<>(maxWidth, totalHeight);
            return _size;
        } else { // ROW
            var totalWidth = 0;
            var maxHeight = 0;
            for (var child : _children) {
                var childSize = child.measure();
                totalWidth += childSize.width();
                maxHeight = Math.max(maxHeight, childSize.height());
            }
            _size = new Size<>(totalWidth, maxHeight);
            return _size;
        }
    }

    @Override
    public void dispose() {
        _children.forEach(UIElement::dispose);
        _children.clear();
        TileUtil.deleteTexture(_bgTexture);
    }

    @Override
    public void onTap() {
        // Ignore for now
    }
}
