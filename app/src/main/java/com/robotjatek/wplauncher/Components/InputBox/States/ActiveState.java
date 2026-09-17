package com.robotjatek.wplauncher.Components.InputBox.States;

import com.robotjatek.wplauncher.Components.InputBox.InputBox;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.IUIContext;

public class ActiveState extends BaseState {

    private final IUIContext _uiContext;
    private final static int BLINK_TIMEOUT = 500; // ms
    private float _blinkTimer = 0;
    private boolean _cursorVisible = true;

    public ActiveState(InputBox context, IUIContext uiContext) {
        super(context);
        _uiContext = uiContext;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        blinkCursor(delta);
    }

    private void blinkCursor(float delta) {
        _blinkTimer += delta;
        if (_blinkTimer > BLINK_TIMEOUT) {
            _cursorVisible = !_cursorVisible;
            _blinkTimer = 0;
            _context.getCursor().setVisible(_cursorVisible);
        }
    }

    @Override
    public void enter() {
        super.enter();
        _uiContext.requestFocus(_context);
        _context.getCursor().setVisible(true);
        _context.showCursor(true);
        _context.getHandle().setVisible(true);
        _context.showHandle(true);
    }

    @Override
    public void exit() {
        super.exit();
        _context.getCursor().setVisible(false);
        _context.showCursor(false);
        _context.showHandle(false);
        _context.getHandle().setVisible(false);
        _context.getOverlay().removeAdorner(_context.getHandle());
    }

    @Override
    public void onFocusLost() {
        _context.changeState(_context.IDLE_STATE());
    }

    @Override
    public void decrementCursorPosition() {
        super.decrementCursorPosition();
        resetBlink();
    }

    @Override
    public void incrementCursorPosition() {
        super.incrementCursorPosition();
        resetBlink();
    }

    @Override
    protected void onTextModified() {
        super.onTextModified();
        resetBlink();
    }

    private void resetBlink() {
        _blinkTimer = 0;
        _cursorVisible = true;
        _context.getCursor().setVisible(true);
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        _context.setCursorPositionWithXPosition(gesture.getX());
        resetBlink();
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _context.setCursorPositionWithXPosition(gesture.getX());
        resetBlink();
        return true;
    }
}
