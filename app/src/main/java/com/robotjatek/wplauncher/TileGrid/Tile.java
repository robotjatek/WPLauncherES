package com.robotjatek.wplauncher.TileGrid;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Components.ITouchable;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TouchHandler;
import com.robotjatek.wplauncher.QuadRenderer;

public class Tile implements ITouchable {
    private final TouchHandler _touchHandler = new TouchHandler(this);
    public static final Size<Integer> SMALL = new Size<>(1, 1);
    public static final Size<Integer> MEDIUM = new Size<>(2, 2);
    public static final Size<Integer> WIDE = new Size<>(4, 2);
    private final float[] _frontViewMatrix = new float[16];
    private final float[] _backViewMatrix = new float[16];
    private final float[] _clipMatrix = new float[16];
    private static final float TIME_BEFORE_FLIP_MIN = 4000f;
    private static final float TIME_BEFORE_FLIP_MAX = 8000f;
    private boolean _disposed = false;
    private Position<Integer> _position;
    private Size<Integer> _size;
    private static final float RESIZE_DURATION = 150f; // ms
    private float _resizeElapsed = 0f;
    private float _startWidth = -1;
    private float _startHeight = -1;
    private float _visualWidth = -1;
    private float _visualHeight = -1;
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

    public float getVisualWidth() {
        return _visualWidth;
    }

    public float getVisualHeight() {
        return _visualHeight;
    }

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
                               Position<Float> offset, TileDrawContext drawContext, QuadRenderer renderer) {
        _touchHandler.update(delta);

        var targetWidth = drawContext.calculateWidth(_size);
        var targetHeight = drawContext.calculateHeight(_size);

        if (_visualWidth < 0) {
            _visualWidth = targetWidth;
            _visualHeight = targetHeight;
            _startWidth = targetWidth;
            _startHeight = targetHeight;
        }

        if (_resizeElapsed < RESIZE_DURATION) {
            _resizeElapsed += delta;
            var t = _resizeElapsed / RESIZE_DURATION;
            var factor = 1 - (1 - t) * (1 - t) * (1 - t);
            _visualWidth = _startWidth + (targetWidth - _startWidth) * factor;
            _visualHeight = _startHeight + (targetHeight - _startHeight) * factor;
        } else {
            _visualWidth = targetWidth;
            _visualHeight = targetHeight;
        }

        var visualScaleX = _visualWidth / targetWidth;
        var visualScaleY = _visualHeight / targetHeight;
        var x = drawContext.xOf(this) + offset.x();
        var y = drawContext.yOf(this) + offset.y();

        updateFlip(delta);

        buildTileMatrix(_frontViewMatrix, viewMatrix, x, y, (int)targetWidth, (int)targetHeight, visualScaleX, visualScaleY, -1.0f, 1f);
        buildTileMatrix(_backViewMatrix, viewMatrix, x, y, (int)targetWidth, (int)targetHeight, visualScaleX, visualScaleY, 1.0f, -1f);

        Matrix.setIdentityM(_clipMatrix, 0);
        Matrix.translateM(_clipMatrix, 0, x, y, 0f);
        Matrix.scaleM(_clipMatrix, 0, _visualWidth, _visualHeight, 1f);
        Matrix.multiplyMM(_clipMatrix, 0, viewMatrix, 0, _clipMatrix, 0);

        renderer.beginClip(projMatrix, _clipMatrix);

        // Render with the new size while scaling it with the gradually smaller visual size
        var logicalSize = new Size<>((int)targetWidth, (int)targetHeight);
        _content.draw(delta, projMatrix, _frontViewMatrix, renderer, this, Position.ZERO, logicalSize);
        if (_backContent != null) {
            _backContent.draw(delta, projMatrix, _backViewMatrix, renderer, this, Position.ZERO, logicalSize);
        }
        renderer.endClip();
    }

    private void updateFlip(float delta) {
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
    }

    private void buildTileMatrix(float[] out, float[] viewMatrix, float x, float y,
                                 int width, int height, float vScaleX, float vScaleY, float offsetZ, float sideScaleY) {
        Matrix.setIdentityM(out, 0);
        Matrix.translateM(out, 0, x + (width * vScaleX) / 2f, y + (height * vScaleY) / 2f, 0f);
        Matrix.rotateM(out, 0, _rot, -1f, 0f, 0f);

        if (sideScaleY != 1f) {
            Matrix.scaleM(out, 0, 1f, sideScaleY, 1f);
        }
        Matrix.scaleM(out, 0, vScaleX * _scale, vScaleY * _scale, 1f);
        Matrix.translateM(out, 0, -width / 2f, -height / 2f, offsetZ);
        Matrix.multiplyMM(out, 0, viewMatrix, 0, out, 0);
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
        _startWidth = _visualWidth;
        _startHeight = _visualHeight;
        _resizeElapsed = 0;
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
