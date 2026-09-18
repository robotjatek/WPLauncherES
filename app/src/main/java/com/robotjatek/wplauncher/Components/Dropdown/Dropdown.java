package com.robotjatek.wplauncher.Components.Dropdown;

import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public class Dropdown implements UIElement, ITouchable {

    private static final int BORDER_SIZE_PX = 4; // TODO: make this DP aware
    private boolean _disposed = false;
    private final TouchHandler _touchHandler = new TouchHandler(this);
    private final AbsoluteLayout _borderLayout = new AbsoluteLayout();
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _label;
    private final Size<Integer> _size;
    private boolean _isDirty = true;

    public Dropdown(Size<Integer> size) {
        _size = size;
        _label = new Label("dark", 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _borderLayout.setBgColor(Colors.WHITE);
        _layout.setBgColor(Colors.BLACK);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _touchHandler.update(delta);

        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if(_isDirty) {
            _isDirty = false;
            _borderLayout.removeChild(_layout);
            _layout.onResize(w - BORDER_SIZE_PX * 2, h - BORDER_SIZE_PX * 2);
            _borderLayout.addChild(_layout, new Position<>((float)BORDER_SIZE_PX, (float)BORDER_SIZE_PX));

            var textOffset = 16f; // TODO: DP aware
            _layout.removeChild(_layout);
            _layout.addChild(_label,
                    new Position<>(textOffset, (h - BORDER_SIZE_PX * 2f) / 2f - _label.measure().height() / 2f));
            _isDirty = false;
        }

        _borderLayout.draw(delta, proj, view, renderer, new Position<>(x, y), new Size<>(w, h));
    }

    @Override
    public Size<Integer> measure() {
        return _size;
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
    public void onPress() {
        _layout.setBgColor(Colors.WHITE);
    }

    @Override
    public void onRelease() {
        _layout.setBgColor(Colors.BLACK);
    }

    @Override
    public void onAction() {
        // TODO: trigger ontap
        // after touch flash is done trigger open menu
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _borderLayout.dispose();
            _disposed = true;
        }
    }
}
