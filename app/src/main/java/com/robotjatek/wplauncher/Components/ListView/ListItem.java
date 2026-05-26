package com.robotjatek.wplauncher.Components.ListView;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public class ListItem<T> {
    private Runnable _onTap;
    private boolean _dirty = true;
    private T _payload;
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _textLabel;
    private Icon _icon;
    private float _scale = 1.0f;
    private static final Size<Integer> DEFAULT_SIZE = new Size<>(96, 96);
    private static final float TAP_ACTION_DELAY_MS = 50f;
    private float _tapDelayRemainingMs = 0f;

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
        updateTapDelay(delta);
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
    public void onPress() {
        cancelPendingTap();
        setScale(0.97f);
    }

    public void onRelease(boolean fireTap) {
        setScale(1f);
        if (fireTap) {
            scheduleTap();
        }
    }

    public void cancelPendingTap() {
        _tapDelayRemainingMs = 0f;
    }

    private void scheduleTap() {
        _tapDelayRemainingMs = TAP_ACTION_DELAY_MS;
    }

    private void updateTapDelay(float delta) {
        if (_tapDelayRemainingMs <= 0f) {
            return;
        }
        _tapDelayRemainingMs -= delta;
        if (_tapDelayRemainingMs <= 0f) {
            _tapDelayRemainingMs = 0f;
            runTapAction();
        }
    }

    private void runTapAction() {
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