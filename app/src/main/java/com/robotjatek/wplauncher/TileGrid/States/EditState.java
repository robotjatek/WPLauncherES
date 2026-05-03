package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IState;
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

    public IState EDIT_IDLE() {
        return new EditIdleState(this, _context);
    }

    public IState EDIT_READY(float x, float y) {
        return new EditReadyState(this, _context, x, y);
    }

    public IState EDIT_DRAG(float x, float y) {
        return new EditDragState(this, _context, x, y);
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    private IState _state;

    private final float _x;
    private final float _y;

    public EditState(TileGrid context, float x, float y) {
        super(context);
        _x = x;
        _y = y;
        _state = EDIT_IDLE();
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
    public boolean handleGesture(Gesture gesture) {
        return _state.handleGesture(gesture);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        _state.update(delta);
    }
}
