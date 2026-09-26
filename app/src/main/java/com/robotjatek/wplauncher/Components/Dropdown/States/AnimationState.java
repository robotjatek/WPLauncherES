package com.robotjatek.wplauncher.Components.Dropdown.States;

import com.robotjatek.wplauncher.Components.Dropdown.Dropdown;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IState;

public class AnimationState<T> implements IState {

    private final Dropdown<T> _context;
    private final Size<Integer> _targetSize;
    private static final float DURATION = 200; // milliseconds
    private float _elapsed = 0f;
    private float _startHeight;

    public AnimationState(Dropdown<T> context, Size<Integer> targetSize) {
        _context = context;
        _targetSize = targetSize;
    }

    @Override
    public void enter() {
        if (_targetSize.equals(_context.getOpenSize())) {
            _context.setOpen(true);
        }
        _startHeight = _context.measure().height();
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

        _elapsed += delta;
        if (_elapsed >= DURATION) {
            _context.setSize(_targetSize);
            _context.changeState(_context.IDLE_STATE());
        } else {
            var t = _elapsed / DURATION;
            var factor = 1 - (1 - t) * (1 - t);
            var currentHeight = (int)(_startHeight + (_targetSize.height() - _startHeight) * factor);
            _context.setSize(new Size<>(_targetSize.width(), currentHeight));
        }
    }
}
