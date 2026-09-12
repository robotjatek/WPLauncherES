package com.robotjatek.wplauncher.Components.InputBox;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

// TODO: move handler? States?
//  set cursor position when moving the handle
// TODO: only show when....
// TODO: align its center to the cursor
// TODO: IGestureOverlay for cursor adorner
//  gesture overlay is the top level gesture receiver, if no hit it passes the gesture down
//  How do i add a leaf to the top-level layer?
//  add tile adorners too later
public class CursorHandle implements UIElement {
    private final float[] _modelMatrix = new float[16];
    private final InputBox _parent;
    private Size<Integer> _size = new Size<>(20, 20); // TODO: size
    private boolean _visible = true;

    public CursorHandle(InputBox parent) {
        _parent = parent;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        if (_visible) {
            var x = drawContext.xOf(this);
            var y = drawContext.yOf(this);
            var w = (int) drawContext.widthOf(this);
            var h = (int) drawContext.heightOf(this);

            Matrix.setIdentityM(_modelMatrix, 0);
            Matrix.translateM(_modelMatrix, 0, x, y, 0f);
            Matrix.scaleM(_modelMatrix, 0, w, h, 1f);
            Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);

            renderer.drawFlat(proj, _modelMatrix, Colors.WHITE); // TODO: something textured
        }
    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    public void setSize(Size<Integer> size) {
        if (_size == size) return;

        _size = size;
    }

    public void setVisible(boolean visible) {
        _visible = visible;
    }


    @Override
    public boolean handleMove(MoveGesture gesture) {
        _parent.setCursorPosition(gesture.getX());
        return true;
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        return true;
    }

    @Override
    public void dispose() {
    }
}
