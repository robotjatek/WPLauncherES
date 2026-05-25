package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.TileGrid.States.IdleStates.IdlePressState;
import com.robotjatek.wplauncher.TileGrid.States.IdleStates.IdleReadyState;
import com.robotjatek.wplauncher.TileGrid.States.IdleStates.IdleReleaseState;
import com.robotjatek.wplauncher.TileGrid.Tile;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

/**
 * Hierarchical state that manages the "idle interaction" behavior of the tile grid.
 * This state is itself a state machine that delegates to one of several substates
 * depending on user interaction.
 * It is responsible for:
 * - enter/exit/update delegation
 * - Routing gestures to the active substate
 * - Coordinating transitions between idle interaction phases
 * Substates:
 * <ul>
 *   <li>{@link IdleReadyState} - default state, waiting for input</li>
 *   <li>{@link IdlePressState} - finger down with delayed press activation and movement cancellation</li>
 *   <li>{@link IdleReleaseState} - finger released, ensures press visibility timing and triggers launch</li>
 * </ul>
 */
public class IdleState extends BaseState {

    private IState _state;

    public IState READY() {
        return new IdleReadyState(this, _context);
    }

    public IState PRESS(Tile tile, float downX, float downY) {
        return new IdlePressState(this, _context, tile, downX, downY);
    }

    public IState RELEASE(Tile tile, boolean pressAlreadyVisible) {
        return new IdleReleaseState(this, _context, tile, pressAlreadyVisible);
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public IdleState(TileGrid context) {
        super(context);
        _state = READY();
    }

    @Override
    public void enter() {
        super.enter();
        _state.enter();
    }

    @Override
    public void exit() {
        _state.exit();
        super.exit();
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        _state.update(delta);
    }
}
