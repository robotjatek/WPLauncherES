package com.robotjatek.wplauncher.Components.Button.States;

import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.IState;

public abstract class ButtonBaseState implements IState {

    protected Button _context;

    public ButtonBaseState(Button context) {
        _context = context;
    }

    @Override
    public void enter() {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void exit() {

    }

    protected void abortPress(boolean pressed) {
        // TODO: tuti kell ez a két külön ág? nem lehet egybe? onRelease(false) mindig cancelezzen?
        if (pressed) {
            // Cancel the visual cue if the item was only pressed for a brief period and do NOT schedule the tap event
            _context.onRelease(false);
        } else {
            _context.cancelPendingTap();
        }
    }
}
