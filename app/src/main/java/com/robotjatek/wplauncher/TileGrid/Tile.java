package com.robotjatek.wplauncher.TileGrid;

import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IDrawContext;
import com.robotjatek.wplauncher.QuadRenderer;

public class Tile {
    public static final Size<Integer> SMALL = new Size<>(1, 1);
    public static final Size<Integer> MEDIUM = new Size<>(2, 2);
    public static final Size<Integer> WIDE = new Size<>(4, 2);
    private static final float TIME_BEFORE_FLIP_MIN = 4000f;
    private static final float TIME_BEFORE_FLIP_MAX = 8000f;
    private Position<Integer> _position;
    private Size<Integer> _size;
    public String title;
    private final App _app;
    public int bgColor;
    private final DragInfo _dragInfo = new DragInfo();
    private final ITileContent _content;
    private final ITileContent _backContent;
    private float _scale = 1.0f;
    private float _rot = 180f;
    private float _targetRot = 180f;
    private float _timeOnSide = 0f;
    private float _flipInterval;

    public Tile(Position<Integer> position, Size<Integer> size, String title, App app, int bgColor, ITileContent content, ITileContent backContent) {
        _position = position;
        _size = size;
        this.title = title;
        this.bgColor = bgColor;
        _app = app;
        _content = content;
        _backContent = backContent;
        _flipInterval = TIME_BEFORE_FLIP_MIN +
                (float) (Math.random() * (TIME_BEFORE_FLIP_MAX - TIME_BEFORE_FLIP_MIN));
    }

    /**
     * Draw matrix with an offset of its original position. Scaling can be applied
     */
    public void drawWithOffset(float delta, float[] projMatrix, float[] viewMatrix,
                               Position<Float> offset, IDrawContext<Tile> drawContext, QuadRenderer renderer) {
        var width = (int) (drawContext.widthOf(this) * _scale);
        var height = (int) (drawContext.heightOf(this) * _scale);
        var xDiff = (width - drawContext.widthOf(this)) / 2; // correction for the scaling
        var yDiff = (height - drawContext.heightOf(this)) / 2; // correction for the scaling

        var correctedX = drawContext.xOf(this) + offset.x() - xDiff; // x corrected by the scaling and the offset
        var correctedY = drawContext.yOf(this) + offset.y() - yDiff; // y corrected by the scaling and the offset

        // fake perspective
        var scaleY = (float) Math.cos(Math.toRadians(_rot));

        var frontViewMatrix = new float[16];
        Matrix.setIdentityM(frontViewMatrix, 0);
        Matrix.translateM(frontViewMatrix, 0, correctedX + width / 2f, correctedY + height/2f, 0f);
        Matrix.scaleM(frontViewMatrix, 0, 1, scaleY, 1);
        Matrix.translateM(frontViewMatrix, 0, -width / 2f, -height / 2f, 0f);
        Matrix.multiplyMM(frontViewMatrix, 0, viewMatrix, 0, frontViewMatrix, 0);

        var backViewMatrix = new float[16];
        Matrix.setIdentityM(backViewMatrix, 0);
        Matrix.translateM(backViewMatrix, 0, correctedX + width / 2f, correctedY + height / 2f, 0f);
        // 2 combined scales: first mirror, then rotate
        Matrix.scaleM(backViewMatrix, 0, 1, -1, 1); // Mirror the back side
        Matrix.scaleM(backViewMatrix, 0, 1, scaleY, 1); // Then apply rotation scale
        Matrix.translateM(backViewMatrix, 0, -width / 2f, -height / 2f, 0f);
        Matrix.multiplyMM(backViewMatrix, 0, viewMatrix, 0, backViewMatrix, 0);

        var backHasContent = _backContent != null && _backContent.hasContent() && !_size.equals(Tile.SMALL);
        if (backHasContent) {
            _timeOnSide += delta;
            if (_timeOnSide >= _flipInterval) {
                _timeOnSide = 0;
                _targetRot = (_targetRot == 0f) ? 180f : 0f;
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

        // Draw only the visible side
        if (scaleY >= 0) {
            _content.draw(delta, projMatrix, frontViewMatrix, renderer, this, new Position<>(0f, 0f), new Size<>(width, height));
        } else {
            if (_backContent != null) {
                _backContent.draw(delta, projMatrix, backViewMatrix, renderer, this, new Position<>(0f, 0f), new Size<>(width, height));
            }
        }
    }

    public void onTap() {
        if (_app != null) {
            _app.action().run();
        }
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

    public void dispose() {
        _content.dispose();
        if (_backContent != null) {
            _backContent.dispose();
        }
    }
}
