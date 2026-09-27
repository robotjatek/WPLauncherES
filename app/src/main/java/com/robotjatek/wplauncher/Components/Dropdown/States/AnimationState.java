package com.robotjatek.wplauncher.Components.Dropdown.States;

import com.robotjatek.wplauncher.Components.Dropdown.Dropdown;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IState;

public class AnimationState<T> implements IState {

    private final Dropdown<T> _context;
    private final Size<Integer> _targetSize;
    private static final float DURATION = 250; // milliseconds
    private float _elapsed = 0f;
    private float _startHeight;
    private int _selectedIndex = -1;

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
        _selectedIndex = _context.getSelectedIndex();
        if (_selectedIndex < 0) {
            _selectedIndex = 0;
        }
    }

    @Override
    public void exit() {
        if (_targetSize.equals(_context.getClosedSize())) {
            _context.setOpen(false);
        }
        _context.setAnimationOffset(0f);
    }

    @Override
    public void update(float delta) {
        _context.getTouchhandler().update(delta);

        _elapsed += delta;
        if (_elapsed >= DURATION) {
            _context.setSize(_targetSize);
            _context.changeState(_context.IDLE_STATE());
        } else {
            // Animate dropdown height
            var t = _elapsed / DURATION;
            var factor = 1 - (1 - t) * (1 - t) * (1 - t);
            var currentHeight = (int)(_startHeight + (_targetSize.height() - _startHeight) * factor);
            _context.setSize(new Size<>(_targetSize.width(), currentHeight));

            // Animate dropdown content position
            var animOffset = 0f;
            var isOpening = _targetSize.equals(_context.getOpenSize());
            var innerHeight = currentHeight - Dropdown.BORDER_SIZE_PX * 2;

            var itemHeight = _context.getItemHeight();
            if (isOpening) {
                // open animation
                // content stays in place until the dropdown grows to overlap the content bottom
                // then move the content together with the expanding dropdown bottom
                var contentBottom = (_context.getModel().size() - _selectedIndex) * itemHeight;
                if (innerHeight < contentBottom) {
                    // layout stays in place
                    animOffset = -_selectedIndex * itemHeight;
                } else {
                    // layout moves with the dropdown
                    var totalContentHeight = _context.getModel().size() * itemHeight;
                    animOffset = innerHeight - totalContentHeight;
                }
            } else {
                // close animation
                // content stays in place until the shrinking bottom reaches the bottom of the selected item
                // then move the content layer together with the shrinking dropdown bottom
                var selectedBottom = (_selectedIndex + 1) * itemHeight;
                if (innerHeight >= selectedBottom) {
                    // layout stays in place
                    animOffset = 0;
                } else {
                    // layout moves with the bottom
                    animOffset = innerHeight - selectedBottom;
                }
            }

            _context.setAnimationOffset(animOffset);
        }
    }
}
