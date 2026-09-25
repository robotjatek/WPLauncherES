package com.robotjatek.wplauncher.Components.Dropdown.States;

import com.robotjatek.wplauncher.Components.Dropdown.Dropdown;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IState;

public class AnimationState<T> implements IState {

    private final Dropdown<T> _context;
    private final Size<Integer> _targetSize;

    public AnimationState(Dropdown<T> context, Size<Integer> targetSize) {
        _context = context;
        _targetSize = targetSize;
    }

    @Override
    public void enter() {
        if (_targetSize.equals(_context.getOpenSize())) {
            _context.setOpen(true);
        }
    }

    @Override
    public void exit() {
        if (_targetSize.equals(_context.getClosedSize())) {
            _context.setOpen(false);
        }
    }

    @Override
    public void update(float delta) {
        _context.getTouchhandler().update(delta);
        // TODO: animate size

        var currentSize = _context.measure();
        if (currentSize.equals(_targetSize)) {
            _context.changeState(_context.IDLE_STATE());
            return;
        }

        // TODO: lerp
        _context.setSize(_targetSize); // TODO: proper animation
    }
}
