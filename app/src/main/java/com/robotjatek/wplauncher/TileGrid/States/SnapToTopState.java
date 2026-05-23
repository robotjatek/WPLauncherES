package com.robotjatek.wplauncher.TileGrid.States;

import com.robotjatek.wplauncher.TileGrid.TileGrid;

public class SnapToTopState extends BaseState {
    private final float _startScrollY;
    private static final float DURATION = 150; // milliseconds
    private float _smoothDelta = 0f;
    private float _elapsed = 0f;

    public SnapToTopState(TileGrid context) {
        super(context);
        _startScrollY = context.getScroll().getScrollOffset();
    }

    @Override
    public void update(float delta) {
        _smoothDelta = _smoothDelta * 0.8f + delta * 0.2f;
        _elapsed += _smoothDelta;
        var scroll = _context.getScroll();
        if (_elapsed >= DURATION) {
            // snap to 0 when animation is done
            scroll.setScrollOffset(0);
            _context.changeState(_context.IDLE_STATE());
        } else {
            var t = _elapsed / DURATION;
            var factor = 1 - (1 - t) * (1 - t) * (1 - t);
            scroll.setScrollOffset(_startScrollY * (1 - factor));
        }
    }
}
