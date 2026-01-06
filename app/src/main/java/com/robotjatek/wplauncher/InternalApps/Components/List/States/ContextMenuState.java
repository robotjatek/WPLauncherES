package com.robotjatek.wplauncher.InternalApps.Components.List.States;

import com.robotjatek.wplauncher.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.InternalApps.Components.List.ListView;

public class ContextMenuState<T> extends BaseState<T> {

    private final float _x;
    private final float _y;
    private ContextMenu<T> _menu;
    private boolean _acceptsInput = false;

    public ContextMenuState(ListView<T> context, float x, float y) {
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
