package com.robotjatek.wplauncher.TileGrid.States.IdleStates;

import com.robotjatek.wplauncher.TileGrid.States.IdleState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

/**
 * State entered after the user releases a tile.
 * Ensures that the tile's pressed visual state remains visible for a minimum
 * amount of time before the release animation is played.
 * If the pressed visual state was not yet shown when the finger was released,
 * {@link Tile#onPress()} is called on entry and the state waits for
 * {@code MIN_PRESS_VISIBLE_MS} before releasing.
 * Once the minimum visibility time has elapsed,
 * {@link Tile#onRelease(boolean)} is called and the state transitions back to
 * {@link IdleReadyState}.
 * If the state is exited before the release occurs, the pressed visual state
 * is canceled to ensure the tile returns to its normal appearance.
 */
public class IdleReleaseState extends IdleBaseState {

    private static final float MIN_PRESS_VISIBLE_MS = 90f;
    private final Tile _tile;
    private final boolean _pressAlreadyVisible;
    private float _holdRemainingMs;
    private boolean _finished;

    public IdleReleaseState(IdleState idle, TileGrid tileGrid, Tile tile, boolean pressAlreadyVisible) {
        super(idle, tileGrid);
        _tile = tile;
        _pressAlreadyVisible = pressAlreadyVisible;
    }

    @Override
    public void enter() {
        if (!_pressAlreadyVisible) {
            _tile.onPress();
        }
        _holdRemainingMs = _pressAlreadyVisible ? 0f : MIN_PRESS_VISIBLE_MS;
        _finished = false;
    }

    @Override
    public void exit() {
        if (!_finished) {
            abortPress(_tile, true);
        }
    }

    @Override
    public void update(float delta) {
        _holdRemainingMs -= delta;
        if (_holdRemainingMs <= 0f) {
            _tile.onRelease(true);
            _finished = true;
            _idle.changeState(_idle.READY());
        }
    }
}
