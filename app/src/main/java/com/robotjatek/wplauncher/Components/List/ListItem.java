package com.robotjatek.wplauncher.Components.List;

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
    private final Icon _icon;

    public ListItem(String label, Drawable icon, int iconBgColor, Runnable onTap, T payload) {
        _onTap = onTap;
        _payload = payload;
        _textLabel = new Label(label, 60, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT);
        _icon = new Icon(icon, iconBgColor);
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
        var w = (int) context.widthOf(this);
        var h = (int) context.heightOf(this);
        _layout.draw(delta, projMatrix, viewMatrix, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    public void update(IDrawContext<ListItem<T>> context) {
        if (_dirty) {
            var w = (int) context.widthOf(this);
            var h = (int) context.heightOf(this);
            _icon.setSize(new Size<>(h, h));
            _textLabel.setMaxWidth(w - h);

            var labelX = _icon.measure().width() + w * 0.02f;
            var labelY = (h - _textLabel.measure().height()) / 2f;


            _layout.removeChild(_icon);
            _layout.addChild(_icon, new Position<>(0f, 0f));
            _layout.removeChild(_textLabel);
            _layout.addChild(_textLabel, new Position<>(labelX, labelY));
            _dirty = false;
        }
    }

    public void onTap() {
        if (_onTap != null) {
            _onTap.run();
        }
    }

    public void setDirty() {
        _dirty = true;
    }

    public void dispose() {
        _layout.dispose();
    }
}