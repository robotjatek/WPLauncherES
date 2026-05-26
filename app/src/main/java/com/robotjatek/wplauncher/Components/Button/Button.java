package com.robotjatek.wplauncher.Components.Button;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.BitmapUtil;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Button.States.IdleState;
import com.robotjatek.wplauncher.Components.Button.States.PressedState;
import com.robotjatek.wplauncher.Components.Button.States.ReleaseState;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.HorizontalAlign;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

public class Button implements UIElement {

    private boolean _disposed = false;
    private final float[] _modelMatrix = new float[16];
    private String _text;
    private Drawable _icon;
    private final Runnable _onTap;
    private int _textTexture = -1;
    private int _iconTexture = -1;
    private boolean _isDirty = true;
    private static final float TAP_ACTION_DELAY_MS = 50f;
    private float _tapDelayRemainingMs = 0f;
    private int _textBgColor = Colors.BLACK;
    private int _textColor = Colors.WHITE;

    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public IState PRESSED_STATE(float downX, float downY) {
        return new PressedState(this, downX, downY);
    }

    public IState RELEASE_STATE(boolean pressAlreadyVisible) {
        return new ReleaseState(this, pressAlreadyVisible);
    }

    private IState _state = IDLE_STATE();

    public Button(String text, Drawable icon, Runnable onTap) {
        _text = text;
        _icon = icon;
        _onTap = onTap;
    }

    @Override
    public void draw(float delta, float[] proj, float[] view, IDrawContext<UIElement> drawContext, QuadRenderer renderer) {
        _state.update(delta);
        updateTapDelay(delta);

        var x = drawContext.xOf(this);
        var y = drawContext.yOf(this);
        var w = (int) drawContext.widthOf(this);
        var h = (int) drawContext.heightOf(this);

        if (_isDirty) {
            // 1 px white border
            TileUtil.deleteTexture(_textTexture);
            TileUtil.deleteTexture(_iconTexture);
            _textTexture = TileUtil.createTextTexture(_text, w - 1, h - 1, 48, Typeface.BOLD, _textColor, _textBgColor, HorizontalAlign.LEFT, VerticalAlign.CENTER);
            if (_icon != null) {
                _iconTexture = BitmapUtil.createTextureFromDrawable(_icon, h, h);
            }
            _isDirty = false;
        }

        // Border:
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x, y, 0);
        Matrix.scaleM(_modelMatrix, 0, w, h, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.drawFlat(proj, _modelMatrix, Colors.WHITE);

        // text:
        var textOffset = 0;
        if (_icon != null) {
            textOffset = h;
        }
        Matrix.setIdentityM(_modelMatrix, 0);
        Matrix.translateM(_modelMatrix, 0, x+4+textOffset, y+4, 0);
        Matrix.scaleM(_modelMatrix, 0, w-8-textOffset, h-8, 0);
        Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
        renderer.draw(proj, _modelMatrix, _textTexture);

        // draw icon
        if (_icon != null) {
            Matrix.setIdentityM(_modelMatrix, 0);
            Matrix.translateM(_modelMatrix, 0, x+4, y+4, 0);
            Matrix.scaleM(_modelMatrix, 0, h, h-8, 0);
            Matrix.multiplyMM(_modelMatrix, 0, view, 0, _modelMatrix, 0);
            renderer.draw(proj, _modelMatrix, _iconTexture);
        }
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    /**
     * Shrinks the item and cancels any pending tap event
     */
    public void onPress() {
        cancelPendingTap();
        _textBgColor = Colors.WHITE;
        _isDirty = true;
    }

    public void onRelease(boolean fireTap) {
        _textBgColor = Colors.BLACK;
        _isDirty = true;
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
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
    }

    @Override
    public Size<Integer> measure() {
        return new Size<>(0, 100); // TODO: configure height
    }

    public void setText(String text) {
        _text = text;
        _isDirty = true;
    }

    public void setIcon(Drawable icon) {
        _icon = icon;
        _isDirty = true;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            TileUtil.deleteTexture(_textTexture);
            _textTexture = -1;
            TileUtil.deleteTexture(_iconTexture);
            _iconTexture = -1;
            _disposed = true;
        }
    }
}
