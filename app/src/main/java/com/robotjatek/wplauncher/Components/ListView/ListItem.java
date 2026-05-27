package com.robotjatek.wplauncher.Components.ListView;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public class ListItem<T> implements ITouchable {
    private final TouchHandler _touchHandler = new TouchHandler(this);
    public TouchHandler getTouchHandler() {
        return _touchHandler;
    }
    private Runnable _onTap;
    private boolean _dirty = true;
    private T _payload;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _textLabel;
    private Icon _icon;
    private float _scale = 1.0f;
    private static final Size<Integer> DEFAULT_SIZE = new Size<>(96, 96);

    public ListItem(String label, Drawable icon, int iconBgColor, Runnable onTap, T payload) {
        _onTap = onTap;
        _payload = payload;
        _textLabel = new Label(label, 60, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
        if (icon != null) {
            _icon = new Icon(icon, iconBgColor, DEFAULT_SIZE);
        }
    }

    public String getLabel() {
        return _textLabel.getText();
    }

    public void setLabel(String label) {
        _textLabel.setText(label);
    }

    public void setIconBgColor(int color) {
        _icon.setBgColor(color);
    }

    public T getPayload() {
        return _payload;
    }

    public void setPayload(T payload) {
        _payload = payload;
    }

    public void setIcon(Drawable icon) {
        _icon.setIconDrawable(icon);
    }

    public void setOnTap(Runnable onTap) {
        _onTap = onTap;
    }

    public void draw(float delta, float[] projMatrix, float[] viewMatrix, IDrawContext<ListItem<T>> context, QuadRenderer renderer) {
        var x = context.xOf(this);
        var y = context.yOf(this);
        var w = (int) (context.widthOf(this) * _scale);
        var h = (int) (context.heightOf(this) * _scale);
        _layout.draw(delta, projMatrix, viewMatrix, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    public void update(float delta, IDrawContext<ListItem<T>> context) {
        _touchHandler.update(delta);
        if (_dirty) {
            var w = (int) (context.widthOf(this) * _scale);
            var h = (int) (context.heightOf(this) * _scale);
            var xDiff = (w - context.widthOf(this)) / 2; // correction for the scaling
            var yDiff = (h - context.heightOf(this)) / 2; // correction for the scaling

            _textLabel.setMaxWidth(w - h);
            _textLabel.setTextSize((int)(60 * _scale));
            var labelX = w * 0.02f;
            var labelY = (h - _textLabel.measure().height()) / 2f;

            if (_icon != null) {
                _icon.setSize(new Size<>(h, h));
                labelX = _icon.measure().width() + w * 0.02f;
                _layout.removeChild(_icon);
                _layout.addChild(_icon, new Position<>(-xDiff, -yDiff));
            }

            _layout.removeChild(_textLabel);
            _layout.addChild(_textLabel, new Position<>(-xDiff + labelX, -yDiff + labelY));
            _dirty = false;
        }
    }

    /**
     * Shrinks the item and cancels any pending tap event
     */
    @Override
    public void onPress() {
        setScale(0.97f);
    }

    @Override
    public void onRelease() {
        setScale(1f);
    }

    @Override
    public void onAction() {
        if (_onTap != null) {
            _onTap.run();
        }
    }

    public void setScale(float scale) {
        _scale = scale;
        _dirty = true;
    }

    public void setDirty() {
        _dirty = true;
    }

    public void dispose() {
        _layout.dispose();
    }
}