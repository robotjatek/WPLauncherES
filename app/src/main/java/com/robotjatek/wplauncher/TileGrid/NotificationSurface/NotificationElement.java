package com.robotjatek.wplauncher.TileGrid.NotificationSurface;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TextBlock.TextBlock;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public class NotificationElement implements UIElement {

    private boolean _disposed = false;
    private boolean _dirty = true;
    private final StackLayout _layout = new StackLayout();
    private final Label _titleLabel = new Label("Should not be seen", 56, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
    private final TextBlock _textBlock = new TextBlock("", 52, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT, 400);
    private Size<Integer> _size = new Size<>(0, 0);
    private int _padding = 0;

    public NotificationElement() {
        _layout.addChild(_titleLabel);
        _layout.addChild(_textBlock);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_dirty) {
            _titleLabel.setMaxWidth(_size.width() - _padding);
            _textBlock.setMaxWidth(_size.width() - _padding);
            _textBlock.setMaxHeight(_size.height() - _titleLabel.measure().height() - _padding);
            _layout.onResize(_size.width(), _size.height());
            _dirty = false;
        }

        _layout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    public void setContent(String title, String text) {
        _titleLabel.setText(title);
        _textBlock.setText(text);
        _dirty = true;
    }

    public void setPadding(int padding) {
        _layout.setPadding(padding);
        _padding = padding;
        _dirty = true;
    }

    public void setBgColor(int color) {
        _layout.setBgColor(color);
        _dirty = true;
    }

    public void setSize(Size<Integer> size) {
        if (_size.equals(size)) return;
        _size = size;
        _dirty = true;
    }

    @Override
    public Size<Integer> measure() {
        return _layout.measure();
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
