package com.robotjatek.wplauncher.Components.InputBox;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

// TODO: States?
// TODO: make the gesture boundaries larger than the visual boundaries
// TODO: add tile adorners to the new overlay later too
// TODO: add the ability for UIElements or layouts to completely consume all touch events
public class CursorHandle implements UIElement {
    private final float[] _modelMatrix = new float[16];
    private final InputBox _parent;
    private Size<Integer> _size = new Size<>(250, 100); // TODO: size
    private boolean _visible = true;
    private float _startX;

    public CursorHandle(InputBox parent) {
        _parent = parent;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        if (_visible) {
            var x = drawContext.xOf(this);
            var y = drawContext.yOf(this);
            var w = drawContext.widthOf(this);
            var h = drawContext.heightOf(this);

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

    public void setVisible(boolean visible) {
        _visible = visible;
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        _startX = gesture.getX();
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        var dx = gesture.getX() - _startX;
        applyMovementToCursorPosition(dx);
        return true;
    }

    /**
     * Apply the gesture to the cursor position:
     * - get the current cursor X
     * - add dx to currentX
     * - calculate the new cursor position based on the newX
     * - compare new cursor position with the current cursor position
     * - if they are different, set the new cursor position
     */
    private void applyMovementToCursorPosition(float dx) {
        var currentX = _parent.getCursorXOnCurrentPosition();
        if (Math.abs(dx) > 0) {
            var newX = currentX + dx;
            var newCursorPosition = _parent.calculateCursorPositionOnX(newX);
            if (newCursorPosition != _parent.getCursorPosition()) {
                _parent.setCursorPositionWithXPosition(newX);
            }
        }
    }

    @Override
    public void dispose() {
    }
}
