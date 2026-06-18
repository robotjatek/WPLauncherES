package com.robotjatek.wplauncher.Services.ScreenNavigator.States;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Services.ScreenNavigator.ScreenNavigator;

public class ClosingModalState extends BaseState {

    private static final float DURATION = 250; // milliseconds
    private float _elapsed = 0f;
    private float _smoothDelta = 0f;

    public ClosingModalState(ScreenNavigator context) {
        super(context);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        _smoothDelta = _smoothDelta * 0.8f + delta * 0.2f;
        _elapsed += _smoothDelta;
        if (_elapsed >= DURATION) {
            _context.getModal().setVerticalTranslation(-_context.getModal().getSize().height());
            _context.disposeModal();
            _context.changeState(_context.IDLE_STATE());
        } else {
            // animation
            var t = _elapsed / DURATION;
            var factor = 1 - (1 - t) * (1 - t) * (1 - t);
            _context.getModal().setVerticalTranslation(_context.getModal().getSize().height() * (-factor));
        }
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return false; // dismissing every gesture in this state
    }
}
