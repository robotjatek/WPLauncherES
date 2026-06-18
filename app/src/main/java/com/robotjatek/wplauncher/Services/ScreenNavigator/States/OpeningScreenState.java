package com.robotjatek.wplauncher.Services.ScreenNavigator.States;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.Services.ScreenNavigator.ScreenNavigator;

public class OpeningScreenState extends BaseState {

    private final IScreen _screen;
    private float _translation;
    private static final float DURATION = 10000; // milliseconds
    private float _elapsed = 0f;
    private float _smoothDelta = 0f;

    public OpeningScreenState(ScreenNavigator context, IScreen screen) {
        super(context);
        _screen = screen;
    }

    @Override
    public void enter() {
        super.enter();
        var width = _context.getWidth();
        _screen.onResize(width, _context.getHeight());
        _translation = width;
        _context.setAnimatedScreenTranslation(_translation);
        _context.setAnimatedScreen(_screen);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        _smoothDelta = _smoothDelta * 0.8f + delta * 0.2f;
        _elapsed += _smoothDelta;
        if (_elapsed >= DURATION) {
            _context.setAnimatedScreenTranslation(0);
            _context.changeState(_context.IDLE_STATE());
        } else {
            var t = _elapsed / DURATION;
            var factor = 1 - (1 - t) * (1 - t) * (1 - t);
            _context.setAnimatedScreenTranslation(_translation * (1 - factor));
        }
    }

    @Override
    public void exit() {
        _context.pushToNavigationStack(_screen);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return false; // discard gestures while animating
    }
}
