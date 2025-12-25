package com.robotjatek.wplauncher.AppList.States;

import com.robotjatek.wplauncher.AppList.AppList;
import com.robotjatek.wplauncher.ContextMenu.ContextMenu;

public class ContextMenuState extends BaseState {

    private final float _x;
    private final float _y;
    private ContextMenu _menu;
    private boolean _acceptsInput = false;

    public ContextMenuState(AppList context, float x, float y) {
        super(context);
        _x = x;
        _y = y;
    }

    @Override
    public void enter() {
        super.enter();
        var tappedItem = getItemAt(_y);
        tappedItem.ifPresent(i -> {
            _context.closeContextMenu();
            _menu = _context.openContextMenu(_x, _y, i.getPayload());
        });
    }

    @Override
    public void handleTouchEnd(float x, float y) {
        if (!_acceptsInput) {
            // im already in this state before releasing the finger, so i have to ignore the first release
            _acceptsInput = true;
        } else {
            if (_menu.isTappedOn(x, y)) {
                _menu.onTap(x, y);
            }
            _context.closeContextMenu();
            _context.changeState(_context.IDLE_STATE());
        }
    }
}
