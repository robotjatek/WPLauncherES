package com.robotjatek.wplauncher.StartScreen.States;

import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.StartScreen.StartScreen;

public class SnapState extends BaseState {

    private final float _startOffset;
    private static final float DURATION = 150; // milliseconds
    private float _elapsed = 0f;
    private float _smoothDelta = 0f;

    public SnapState(StartScreen context) {
        super(context);
        _startOffset = context.getPageOffset();
    }

    @Override
    public void update(float delta) {
        //super.update(delta);
        _smoothDelta = _smoothDelta * 0.8f + delta * 0.2f;
        _elapsed += _smoothDelta;
        if (_elapsed >= DURATION) {
            // snap to 0 when animation is done
            _context.setPageOffset(0);
            _context.changeState(_context.IDLE_STATE());
        } else {
            var t = _elapsed / DURATION;
            var factor = 1 - (1 - t) * (1 - t) * (1 - t);
            _context.setPageOffset(_startOffset * (1 - factor));
        }
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        _context.changeState(_context.SWIPE_STATE(_context.getPageOffset()));
        return _context.handleGesture(gesture); // pass gesture to the new state
    }
}
