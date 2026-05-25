package com.robotjatek.wplauncher.TileGrid.States.IdleStates;

import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.TileGrid.States.EditState;
import com.robotjatek.wplauncher.TileGrid.States.IdleState;
import com.robotjatek.wplauncher.TileGrid.States.ScrollState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

/**
 * State entered when the user presses a tile.
 * The tile remains visually unchanged for a short delay. Once the delay elapses,
 * {@link Tile#onPress()} is called and the tile enters its pressed visual state.
 * Releasing the finger transitions to {@link IdleReleaseState}. If the pressed
 * visual state was shown, the release animation will be played.
 * Moving beyond the movement threshold, scrolling, or performing a long press
 * cancels the press. If the tile was already in its pressed visual state, that
 * state is first reverted before transitioning away.
 * Transitions:
 * <ul>
 *     <li> {@link IdleReleaseState} on finger up </li>
 *     <li> {@link ScrollState} on scroll </li>
 *     <li> {@link EditState} on long press </li>
 *     <li> {@link IdleReadyState} if movement exceeds the threshold </li>
 * </ul>
 */
public class IdlePressState extends IdleBaseState {
    private static final float PRESS_DELAY_MS = 100f;
    private static final float MOVE_DELAY_PX = 16f;
    private static final float MOVE_DELAY_SQUARED = MOVE_DELAY_PX * MOVE_DELAY_PX;
    private final Tile _tile;
    private final float _downX;
    private final float _downY;
    private float _pressDelayRemainingMs;
    private boolean _shrunk;

    public IdlePressState(IdleState idle, TileGrid tileGrid, Tile tile, float downX, float downY) {
        super(idle, tileGrid);
        _tile = tile;
        _downX = downX;
        _downY = downY;
    }

    @Override
    public void enter() {
        _pressDelayRemainingMs = PRESS_DELAY_MS;
        _shrunk = false;
    }

    @Override
    public void update(float delta) {
        if (_shrunk) {
            return;
        }
        _pressDelayRemainingMs -= delta;
        if (_pressDelayRemainingMs <= 0f) {
            _tile.onPress();
            _shrunk = true;
        }
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        var dx = gesture.getX() - _downX;
        var dy = gesture.getY() - _downY;
        if (dx * dx + dy * dy > MOVE_DELAY_SQUARED) {
            abortPress(_tile, _shrunk);
            _idle.changeState(_idle.READY());
        }
        return true;
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        abortPress(_tile, _shrunk);
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture);
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        abortPress(_tile, _shrunk);
        _context.changeState(_context.EDIT_STATE(gesture.getX(), gesture.getY()));
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _idle.changeState(_idle.RELEASE(_tile, _shrunk));
        return true;
    }
}
