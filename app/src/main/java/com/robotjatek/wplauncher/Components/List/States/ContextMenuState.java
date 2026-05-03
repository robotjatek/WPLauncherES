package com.robotjatek.wplauncher.Components.List.States;

import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.Components.List.ListView;
import com.robotjatek.wplauncher.Gestures.TapGesture;

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
    public void enter() {
        super.enter();
        var tappedItem = getItemAt(_y);
        tappedItem.ifPresent(i -> {
            _context.closeContextMenu();
            _menu = _context.openContextMenu(_x, _y, i.getPayload());
        });
    }

    @Override
    public boolean handleTap(TapGesture gesture) {
        if (_menu.isTappedOn(gesture.getX(), gesture.getY())) {
            _menu.onTap(gesture.getX(), gesture.getY());
        }
        _context.closeContextMenu();
        _context.changeState(_context.IDLE_STATE());

        return true;
    }
}
