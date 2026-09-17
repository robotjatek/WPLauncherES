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
            _context.showCursor(_cursorVisible);
        }
    }

    @Override
    public void enter() {
        super.enter();
        _uiContext.requestFocus(_context);
        _context.showCursor(true);
    }

    @Override
    public void exit() {
        super.exit();
        _context.showCursor(false);
        _context.showHandle(false);
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
        _context.showHandle(false);
    }

    private void resetBlink() {
        _blinkTimer = 0;
        _cursorVisible = true;
        _context.showCursor(true);
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        _context.setCursorPositionWithXPosition(gesture.getX());
        _context.showHandle(true);
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
