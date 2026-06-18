package com.robotjatek.wplauncher.Services.ScreenNavigator.States;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.Services.ScreenNavigator.ScreenNavigator;

public class ClosingScreenState extends BaseState {

    private static final float DURATION = 200; // milliseconds
    private float _elapsed = 0f;
    private float _smoothDelta = 0;
    private final IScreen _screen;

    public ClosingScreenState(ScreenNavigator context, IScreen screen) {
        super(context);
        _screen = screen;
    }

    @Override
    public void enter() {
        super.enter();
        _context.setAnimatedScreen(_screen);
        _context.popFromNavigationStack();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        _smoothDelta = _smoothDelta * 0.8f + delta * 0.2f;
        _elapsed += _smoothDelta;
        if (_elapsed >= DURATION) {
            // done with the animation: snap to position, pop from stack, return to idle
            _context.setAnimatedScreenTranslation(-_context.getWidth());
            _context.changeState(_context.IDLE_STATE());
        } else {
            // animation
            var t = _elapsed / DURATION;
            var factor = 1 - (1 - t) * (1 - t) * (1 - t);
            _context.setAnimatedScreenTranslation(_context.getWidth() * (factor));
        }
    }

    @Override
    public void exit() {
        _context.setAnimatedScreenTranslation(0);
        _context.setAnimatedScreen(null);
        _screen.dispose();
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return false; // dismiss gestures while animating
    }
}
