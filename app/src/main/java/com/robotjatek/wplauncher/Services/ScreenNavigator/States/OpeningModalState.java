package com.robotjatek.wplauncher.Services.ScreenNavigator.States;

import com.robotjatek.wplauncher.Components.Modal.IModal;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.Services.ScreenNavigator.ScreenNavigator;

public class OpeningModalState extends BaseState {

    private float _translationHeight;
    private final IModal _modal;
    private static final float DURATION = 5000; // milliseconds
    private float _elapsed = 0f;
    private float _smoothDelta = 0f;

    public OpeningModalState(ScreenNavigator context, IModal modal) {
        super(context);
        _modal = modal;
    }

    @Override
    public void enter() {
        super.enter();
        var modalHeight = _modal.getSize().height();
        _translationHeight = -modalHeight;
        _modal.setModalTranslationHeight(_translationHeight);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        _smoothDelta = _smoothDelta * 0.8f + delta * 0.2f;
        _elapsed += _smoothDelta;
        if (_elapsed >= DURATION) {
            _modal.setModalTranslationHeight(0);
            _context.changeState(_context.IDLE_STATE());
        } else {
            var t = _elapsed / DURATION;
            var factor = 1 - (1 - t) * (1 - t) * (1 - t);
            _modal.setModalTranslationHeight(_translationHeight * (1 - factor));
        }
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return false; // dismissing every gesture in this state
    }
}
