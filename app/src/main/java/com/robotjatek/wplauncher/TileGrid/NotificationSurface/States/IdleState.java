package com.robotjatek.wplauncher.TileGrid.NotificationSurface.States;

import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.TileGrid.NotificationSurface.NotificationSurface;

public class IdleState implements IState {

    private final NotificationSurface _context;
    private float _notificationTime = 0f;

    public IdleState(NotificationSurface context) {
        _context = context;
    }

    @Override
    public void enter() {
        _notificationTime = 4000;
    }

    @Override
    public void exit() {}

    @Override
    public void update(float delta) {
        _notificationTime -= delta;
        if (_notificationTime < 0 && _context.getNotifications().size() > 1) {
            _context.changeState(_context.SWAP_STATE());
        }
    }
}
