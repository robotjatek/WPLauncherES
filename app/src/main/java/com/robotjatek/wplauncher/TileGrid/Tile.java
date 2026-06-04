package com.robotjatek.wplauncher.TileGrid;

import android.opengl.GLES32;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.QuadRenderer;

public class Tile implements ITouchable {
    private final TouchHandler _touchHandler = new TouchHandler(this);
    public static final Size<Integer> SMALL = new Size<>(1, 1);
    public static final Size<Integer> MEDIUM = new Size<>(2, 2);
    public static final Size<Integer> WIDE = new Size<>(4, 2);
    private final float[] _frontViewMatrix = new float[16];
    private final float[] _backViewMatrix = new float[16];
    private static final float TIME_BEFORE_FLIP_MIN = 4000f;
    private static final float TIME_BEFORE_FLIP_MAX = 8000f;
    private boolean _disposed = false;
    private Position<Integer> _position;
    private Size<Integer> _size;
    private App _app;
    public int bgColor;
    private final DragInfo _dragInfo = new DragInfo();
    private final ITileContent _content;
    private final ITileContent _backContent;
    private float _scale = 1.0f;
    private float _rot = 0;
    private float _targetRot = 0f;
    private float _timeOnSide = 0f;
    private float _flipInterval;

    public Tile(Position<Integer> position, Size<Integer> size, App app, int bgColor, ITileContent content, ITileContent backContent) {
        _position = position;
        _size = size;
        this.bgColor = bgColor;
        _app = app;
        _content = content;
        _backContent = backContent;
        _flipInterval = TIME_BEFORE_FLIP_MIN +
                (float) (Math.random() * (TIME_BEFORE_FLIP_MAX - TIME_BEFORE_FLIP_MIN));
    }

    /**
     * Draw tile with an offset to its original position
     */
    public void drawWithOffset(float delta, float[] projMatrix, float[] viewMatrix,
                               Position<Float> offset, IDrawContext<Tile> drawContext, QuadRenderer renderer) {
        _touchHandler.update(delta);
        var width = (int) (drawContext.widthOf(this) * _scale);
        var height = (int) (drawContext.heightOf(this) * _scale);
        var xDiff = (width - drawContext.widthOf(this)) / 2;
        var yDiff = (height - drawContext.heightOf(this)) / 2;

        var correctedX = drawContext.xOf(this) + offset.x() - xDiff;
        var correctedY = drawContext.yOf(this) + offset.y() - yDiff;

        Matrix.setIdentityM(_frontViewMatrix, 0);
        Matrix.translateM(_frontViewMatrix, 0, correctedX + width / 2f, correctedY + height / 2f, 0f);
        Matrix.rotateM(_frontViewMatrix, 0, _rot, -1f, 0f, 0f);
        Matrix.translateM(_frontViewMatrix, 0, -width / 2f, -height / 2f, 0.1f);
        Matrix.multiplyMM(_frontViewMatrix, 0, viewMatrix, 0, _frontViewMatrix, 0);

        // Back face: Match tile rotation, then flip vertically.
        // Rotation (1 flip) + Scale Y -1 (1 flip) = 2 flips = Clockwise winding (Visible).
        Matrix.setIdentityM(_backViewMatrix, 0);
        Matrix.translateM(_backViewMatrix, 0, correctedX + width / 2f, correctedY + height / 2f, 0f);
        Matrix.rotateM(_backViewMatrix, 0, _rot, -1f, 0f, 0f);
        Matrix.scaleM(_backViewMatrix, 0, 1f, -1f, 1f); // Mirror the back side
        Matrix.translateM(_backViewMatrix, 0, -width / 2f, -height / 2f, -0.1f);
        Matrix.multiplyMM(_backViewMatrix, 0, viewMatrix, 0, _backViewMatrix, 0);

        var backHasContent = _backContent != null && _backContent.hasContent() && !_size.equals(Tile.SMALL);
        if (backHasContent) {
            _timeOnSide += delta;
            if (_timeOnSide >= _flipInterval) {
                _timeOnSide = 0;
                _targetRot += 180f;
                if (_targetRot > 36000f) { // after 200 flips
                    _targetRot -= 36000f;
                    _rot -= 36000f;
                }
                _flipInterval = TIME_BEFORE_FLIP_MIN +
                        (float) (Math.random() * (TIME_BEFORE_FLIP_MAX - TIME_BEFORE_FLIP_MIN));
            }
        } else {
            _targetRot = 0;
            _timeOnSide = 0;
        }

        if (_rot != _targetRot) {
            var diff = _targetRot - _rot;
            var step = 0.3f * delta;

            if (Math.abs(diff) < step) {
                _rot = _targetRot;
            } else {
                _rot += Math.signum(diff) * step;
            }
        }

        var screenX = viewMatrix[12] + correctedX;
        var screenY = viewMatrix[13] + correctedY;
        var glY = (LauncherRenderer.SCREEN_DATA.screenHeight - (screenY + height + LauncherRenderer.SCREEN_DATA.topInset));
        GLES32.glEnable(GLES32.GL_SCISSOR_TEST);
        GLES32.glScissor((int) screenX, (int)glY, width, height);
        _content.draw(delta, projMatrix, _frontViewMatrix, renderer, this, Position.ZERO, new Size<>(width, height));
        if (_backContent != null) {
            _backContent.draw(delta, projMatrix, _backViewMatrix, renderer, this, Position.ZERO, new Size<>(width, height));
        }
        GLES32.glDisable(GLES32.GL_SCISSOR_TEST);
    }

    public String getPackageName() {
        if (_app == null) {
            return "";
        }
        return _app.packageName();
    }

    public App getApp() {
        return _app;
    }

    public void setApp(App app) {
        _app = app;
        _content.forceRedraw();
        if (_backContent != null) {
            _backContent.forceRedraw();
        }
    }

    public void setBgColor(int color) {
        bgColor = color;
        _content.forceRedraw();
        if (_backContent != null) {
            _backContent.forceRedraw();
        }
    }

    public DragInfo getDragInfo() {
        return _dragInfo;
    }

    public Size<Integer> getSize() {
        return _size;
    }

    public void setSize(Size<Integer> size) {
        _size = size;
        _content.forceRedraw();
        if (_backContent != null) {
            _backContent.forceRedraw();
        }
        if (size.equals(Tile.SMALL)) {
            _rot = 0;
            _targetRot = 0;
        }
    }

    public Position<Integer> getPosition() {
        return _position;
    }

    public void setPosition(Position<Integer> position) {
        _position = position;
    }

    public void setScale(float scale) {
        _scale = scale;
        _content.forceRedraw();
        if (_backContent != null) {
            _backContent.forceRedraw();
        }
    }

    public TouchHandler getTouchHandler() {
        return _touchHandler;
    }

    public void onPress() {
        setScale(0.95f);
    }

    @Override
    public void onRelease() {
        setScale(1f);
    }

    @Override
    public void onAction() {
        if (_app != null) {
            _app.action().run();
        }
    }

    public void dispose() {
        if (!_disposed) {
            _content.dispose();
            if (_backContent != null) {
                _backContent.dispose();
            }
            _disposed = true;
        }
    }
}
