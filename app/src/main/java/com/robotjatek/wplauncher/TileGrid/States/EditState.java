package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.IGestureState;
import com.robotjatek.wplauncher.TileGrid.States.EditStates.EditDragState;
import com.robotjatek.wplauncher.TileGrid.States.EditStates.EditIdleState;
import com.robotjatek.wplauncher.TileGrid.States.EditStates.EditReadyState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;

/**
 * State for TileGrid edit mode.
 * Implements a hierarchical state machine, by having and managing its own states inside.
 * Its only purpose to relay the inputs to the current internal state,
 * and keeping the reference of the selected tile
 */
public class EditState extends BaseState {

    public IGestureState EDIT_IDLE(float x, float y) {
        return new EditIdleState(this, _context, x, y);
    }

    public IGestureState EDIT_READY(float x, float y) {
        return new EditReadyState(this, _context, x, y);
    }

    public IGestureState EDIT_DRAG(float x, float y) {
        return new EditDragState(this, _context, x, y);
    }

    public void changeState(IGestureState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    private IGestureState _state;

    private final float _x;
    private final float _y;

    public EditState(TileGrid context, float x, float y) {
        super(context);
        _x = x;
        _y = y;
        _state = EDIT_IDLE(_x, _y);
    }

    @Override
    public void enter() {
        super.enter();
        var tappedTile = getTileAt(_x, _y);
        tappedTile.ifPresentOrElse(t -> {
            t.getDragInfo().start(_x, _y);
            _context.selectTile(t);
        }, () -> _context.changeState(_context.IDLE_STATE()));
    }

    @Override
    public void handleTouchStart(float x, float y) {
        super.handleTouchStart(x, y);
        _state.handleTouchStart(x, y);
    }

    @Override
    public void handleMove(float x, float y) {
        super.handleMove(x, y);
        _state.handleMove(x, y);
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        super.handleTouchEnd(x, y);
        _state.handleTouchEnd(x, y);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        _state.update(delta);
    }
}
