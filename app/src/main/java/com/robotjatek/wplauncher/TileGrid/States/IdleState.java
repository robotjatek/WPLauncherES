package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class IdleState extends BaseState {
    private static final float PRESS_DELAY_MS = 100f; // shrink only after finger rests this long on a tile
    private static final float MIN_PRESS_VISIBLE_MS = 90f; // keep the animation visible for at least this long
    private static final float MOVE_DELAY_PX = 16f; // do not trigger move for at least this much movement
    private static final float MOVE_DELAY_SQUARED = MOVE_DELAY_PX * MOVE_DELAY_PX;

    private Tile _pressedTile;
    private Tile _tilePendingRelease;
    private float _releaseHoldRemainingMs;
    private boolean _pressShown;
    private boolean _cancelled;
    private float _pressDelayRemainingMs;
    private float _downX;
    private float _downY;

    public IdleState(TileGrid context) {
        super(context);
    }

    @Override
    public void update(float delta) {
        if (_tilePendingRelease != null) {
            if (_releaseHoldRemainingMs > 0f) {
                _releaseHoldRemainingMs -= delta;
                return;
            }
            var tile = _tilePendingRelease;
            _tilePendingRelease = null;
            tile.onRelease(true);
            return;
        }

        if (_pressedTile == null || _cancelled || _pressShown) {
            return;
        }

        _pressDelayRemainingMs -= delta;
        if (_pressDelayRemainingMs <= 0f) {
            _pressShown = true;
            _pressedTile.onPress();
        }
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        return true;
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        cancelCandidate();
        _context.changeState(_context.EDIT_STATE(gesture.getX(), gesture.getY()));
        return true;
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        cancelCandidate();
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture);
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        if (_pressedTile == null || _cancelled) {
            return false;
        }

        var dx = gesture.getX() - _downX;
        var dy = gesture.getY() - _downY;
        if (dx * dx + dy * dy > MOVE_DELAY_SQUARED) {
            cancelCandidate();
        }
        return true;
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        cancelCandidate();
        getTileAt(gesture.getX(), gesture.getY()).ifPresent(t -> {
            _pressDelayRemainingMs = PRESS_DELAY_MS;
            _pressedTile = t;
            _pressShown = false;
            _cancelled = false;
            _downX = gesture.getX();
            _downY = gesture.getY();
        });
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        if (_pressedTile != null && !_cancelled) {
            var tile = _pressedTile;
            if (!_pressShown) {
                tile.onPress();
                _releaseHoldRemainingMs = MIN_PRESS_VISIBLE_MS;
            } else {
                _releaseHoldRemainingMs = 0f;
            }
            _tilePendingRelease = tile;
            clearCandidateRefs();
        } else {
            cancelCandidate();
        }
        return true;
    }

    @Override
    public void exit() {
        cancelCandidate();
        super.exit();
    }

    private void cancelCandidate() {
        if (_tilePendingRelease != null) {
            var tile = _tilePendingRelease;
            _tilePendingRelease = null;
            _releaseHoldRemainingMs = 0f;
            tile.onRelease(false);
        }

        if (_pressedTile != null) {
            if (_pressShown) {
                _pressedTile.onRelease(false);
            } else {
                _pressedTile.cancelPendingTap();
            }
        }

        clearCandidateRefs();
    }

    private void clearCandidateRefs() {
        _pressedTile = null;
        _pressDelayRemainingMs = 0f;
        _pressShown = false;
        _cancelled = true;
    }
}
