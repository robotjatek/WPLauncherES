package com.robotjatek.wplauncher.TileGrid.NotificationSurface.States;

import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.TileGrid.NotificationSurface.NotificationSurface;
import com.robotjatek.wplauncher.TileGrid.Position;

public class SwapState implements IState {

    private final NotificationSurface _context;
    private int _nextId = -1;
    private static final float DURATION = 850f;
    private float _elapsed = 0f;
    private float _width = 0f;

    public SwapState(NotificationSurface context) {
        _context = context;
    }

    @Override
    public void enter() {
        var notifications = _context.getNotifications();
        if (notifications.size() < 2) {
            _context.changeState(_context.IDLE_STATE());
            return;
        }

        _nextId = (_context.getCurrentNotificationId() + 1) % _context.getNotifications().size();
        var next = _context.getNotifications().get(_nextId);
        var nextElement = _context.getNextNotification();
        var size = _context.getLastSize();
        nextElement.setContent(next.title(), next.message());
        
        _width = (float) size.width();
        _context.getLayout().addChild(nextElement, new Position<>(_width, 0f));
        _context.setDirty(true);
    }

    @Override
    public void exit() {
        _context.getLayout().setChildPosition(_context.getCurrentNotification(), Position.ZERO);
        _context.getLayout().removeChild(_context.getNextNotification());
    }

    @Override
    public void update(float delta) {
        _elapsed += delta;
        var t = Math.min(_elapsed / DURATION, 1);
        var eased = 1f - (1f - t) * (1f - t) * (1f - t) * (1f - t);
        var dx = eased * _width;

        _context.getLayout().setChildPosition(_context.getCurrentNotification(), new Position<>(-dx, 0f));
        _context.getLayout().setChildPosition(_context.getNextNotification(), new Position<>(_width - dx, 0f));

        if (t >= 1f) {
            _context.setCurrentNotificationId(_nextId);
            _context.swapElements();
            _context.changeState(_context.IDLE_STATE());
        }
    }
}
