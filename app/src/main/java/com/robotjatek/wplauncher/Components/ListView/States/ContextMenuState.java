package com.robotjatek.wplauncher.Components.ListView.States;

import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;

public class ContextMenuState<T> extends BaseState<T> {
    private final float _x;
    private final float _y;
    private ContextMenu<T> _menu;

    public ContextMenuState(ListView<T> context, float x, float y) {
        super(context);
        _x = x;
        _y = y;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (_menu != null && !_menu.isOpened()) {
            _context.changeState(_context.IDLE_STATE());
        }
    }

    @Override
    public boolean handleDown(DownGesture gesture) {
        if (_menu.isTappedOn(gesture.getX(), gesture.getY())) {
            _menu.onDown(gesture.getX(), gesture.getY());
            return true;
        }
        return false;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _menu.onUp();
        return true;
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        _menu.onMove(gesture.getX(), gesture.getY());
        return true;
    }

    @Override
    public void enter() {
        super.enter();
        var tappedItem = getItemAt(_x, _y);
        tappedItem.ifPresentOrElse(i -> {
            _context.closeContextMenu();
            _menu = _context.openContextMenu(_x, _y, i.getPayload());
        }, () -> _context.changeState(_context.IDLE_STATE()));
    }

    @Override
    public void exit() {
        super.exit();
        _context.closeContextMenu();
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        if (!_menu.isTappedOn(gesture.getX(), gesture.getY())) {
            _context.closeContextMenu();
            _context.changeState(_context.IDLE_STATE());
        }

        return true;
    }

    @Override
    public boolean isCatchingGestures() {
        return true;
    }
}
