package com.robotjatek.wplauncher.Components.InputBox;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public class CursorHandle implements UIElement {
    private final float[] _modelMatrix = new float[16];
    private final InputBox _parent;
    private final Size<Integer> _size = new Size<>(200, 100); // TODO: size
    private boolean _visible = true;
    private float _initialFingerX;
    private float _initialCursorX;
    private boolean _draggingStarted = false;
    private static final float TOUCH_DELAY = 20f;

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

            var handleW = w / 15f;
            var handleH = h / 2f;
            var handleX = x + (w / 2f) - (handleW / 2f);

            Matrix.setIdentityM(_modelMatrix, 0);
            Matrix.translateM(_modelMatrix, 0, handleX, y, 0f);
            Matrix.scaleM(_modelMatrix, 0, handleW, handleH, 1f);
            Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);

            renderer.drawFlat(proj, _modelMatrix, Colors.WHITE); // TODO: something textured;
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
        _initialFingerX = gesture.getX();
        _initialCursorX = _parent.getCursorXOnCurrentPosition();
        _draggingStarted = false;
        _parent.getOverlay().lockGestures(this);
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _draggingStarted = false;
        _parent.getOverlay().unlockGestures();
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        var dx = gesture.getX() - _initialFingerX;
        if (!_draggingStarted) {
            if (Math.abs(dx) < TOUCH_DELAY) {
                return true;
            }
            _draggingStarted = true;
        }
        _parent.setCursorPositionWithXPosition(_initialCursorX + dx);
        return true;
    }

    @Override
    public void dispose() {
    }
}