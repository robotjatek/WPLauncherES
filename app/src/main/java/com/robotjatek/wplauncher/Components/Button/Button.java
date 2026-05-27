package com.robotjatek.wplauncher.Components.Button;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public class Button implements UIElement, ITouchable {

    private final TouchHandler _touchHandler = new TouchHandler(this);
    private boolean _disposed = false;
    private final Runnable _onTap;
    private boolean _isDirty = true;
    private static final float TAP_ACTION_DELAY_MS = 50f;
    private float _tapDelayRemainingMs = 0f;
    private final AbsoluteLayout _borderLayout = new AbsoluteLayout();
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _label;
    private Icon _icon;

    public Button(String text, Icon icon, Runnable onTap) {
        _label = new Label(text, 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _icon = icon;
        _onTap = onTap;
        _borderLayout.setBgColor(Colors.WHITE);
        _layout.setBgColor(Colors.BLACK);
    }

    private static final int BORDER_SIZE_PX = 4;

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _touchHandler.update(delta);

        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_isDirty) {
            _borderLayout.removeChild(_layout);
            _layout.onResize(w - BORDER_SIZE_PX * 2, h - BORDER_SIZE_PX * 2);
            _borderLayout.addChild(_layout, new Position<>((float)BORDER_SIZE_PX, (float)BORDER_SIZE_PX));

            var textOffset = 16f;
            _layout.removeChild(_label);

            if (_icon != null) {
                var iconSize = h - BORDER_SIZE_PX * 2 - 16;
                textOffset += iconSize;
                _icon.setSize(new Size<>(iconSize, iconSize));
                _layout.removeChild(_icon);
                _layout.addChild(_icon, new Position<>(BORDER_SIZE_PX * 2f, BORDER_SIZE_PX * 2f));
            }
            _layout.addChild(_label, new Position<>(textOffset, (h-BORDER_SIZE_PX*2f)/2f - _label.measure().height() / 2f));
            _isDirty = false;
        }

        _borderLayout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));

        updateTapDelay(delta);
    }

    /**
     * Shrinks the item and cancels any pending tap event
     */
    public void onPress() {
        cancelPendingTap();
        _layout.setBgColor(Colors.WHITE);
    }

    public void onRelease(boolean fireTap) {
        _layout.setBgColor(Colors.BLACK);
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

    @Override
    public boolean handleDown(DownGesture gesture) {
        _touchHandler.onDown(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _touchHandler.onUp();
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _touchHandler.onMove(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public Size<Integer> measure() {
        return new Size<>(0, 100); // TODO: configure height
    }

    public void setText(String text) {
        _label.setText(text);
    }

    public void setIcon(Icon icon) {
        if (_icon != null) {
            _icon.dispose();
        }
        _icon = icon;
        _isDirty = true;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _borderLayout.dispose();
            _disposed = true;
        }
    }
}
