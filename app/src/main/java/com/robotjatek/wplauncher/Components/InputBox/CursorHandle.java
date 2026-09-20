package com.robotjatek.wplauncher.Components.InputBox;

import android.graphics.drawable.Drawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileUtil;

public class CursorHandle implements UIElement {
    private final float[] _modelMatrix = new float[16];
    private final InputBox _parent;
    private final Size<Integer> _size = new Size<>(200, 100); // Bounding box of the handle
    private boolean _visible = true;
    private float _initialFingerX;
    private float _initialCursorX;
    private boolean _draggingStarted = false;
    private static final float TOUCH_DELAY = 20f;
    private final int _textureId;
    private ILayout _parentLayout;

    public CursorHandle(InputBox parent, Drawable icon) {
        _parent = parent;
        _textureId = BitmapUtil.createTextureFromDrawable(icon, 96, 96);
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        if (_visible) {
            var x = drawContext.xOf(this);
            var y = drawContext.yOf(this);
            var w = drawContext.widthOf(this);

            // Visual size of the handle
            var handleSize = w / 4f;
            var handleX = x + (w / 2f) - (handleSize / 2f); // positioned in the center of the bounding area

            Matrix.setIdentityM(_modelMatrix, 0);
            Matrix.translateM(_modelMatrix, 0, handleX, y - handleSize / 2f, 0f);
            Matrix.scaleM(_modelMatrix, 0, handleSize, handleSize, 1f);
            Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);

            renderer.draw(proj, _modelMatrix, _textureId);
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
    public void setParent(ILayout parent) {
        _parentLayout = parent;
    }

    @Override
    public void dispose() {
        if (_textureId > 0) {
            TileUtil.deleteTexture(_textureId);
        }
    }
}