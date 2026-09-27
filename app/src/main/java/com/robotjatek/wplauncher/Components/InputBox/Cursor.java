package com.robotjatek.wplauncher.Components.InputBox;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Layouts.ILayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public class Cursor implements UIElement {

    private final float[] _modelMatrix = new float[16];
    private Size<Integer> _size = new Size<>(-1, -1);
    private boolean _visible = false;
    private ILayout _parent;

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

            renderer.drawFlat(proj, _modelMatrix, Colors.WHITE);
        }
    }

    @Override
    public Size<Integer> measure() {
        return _size;
    }

    public void setVisible(boolean visible) {
        _visible = visible;
    }

    public void setSize(Size<Integer> size) {
        if (_size.equals(size)) {
            return;
        }
        _size = size;
        if (_parent != null) {
            _parent.layout();
        }
    }

    @Override
    public void setParent(ILayout parent) {
        _parent = parent;
    }

    @Override
    public void dispose() {
    }
}
