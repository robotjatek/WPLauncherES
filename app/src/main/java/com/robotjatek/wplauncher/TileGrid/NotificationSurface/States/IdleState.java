package com.robotjatek.wplauncher.TileGrid.NotificationSurface.States;

import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.TileGrid.NotificationSurface.NotificationSurface;

public class IdleState implements IState {

    private final NotificationSurface _context;
    private long _timeOnNotification = 0;

    public IdleState(NotificationSurface context) {
        _context = context;
    }

    @Override
    public void enter() {
        _timeOnNotification = 0;
    }

    @Override
    public void exit() {

    }

    @Override
    public void update(float delta) {
        var notificationId = _context.getCurrentNotificationId();
        if (_context.isDirty()) {
            var currentNotification = _context.getCurrentNotification();
            var padding = _context.getLastSize().height() * 0.05f;
            currentNotification.setPadding((int)padding);
            currentNotification.setBgColor(_context.getParent().bgColor);
            if (!_context.getNotifications().isEmpty()) {
                var currentNotificationContent = _context.getNotifications().get(notificationId);
               currentNotification.setContent(currentNotificationContent.title(),
                       currentNotificationContent.message());
            }
            var size = _context.getLastSize();
            currentNotification.setSize(size);
            _context.setDirty(false);
        }

        if (_timeOnNotification > 4000 && !_context.getNotifications().isEmpty()) {
            _timeOnNotification = 0;
            // TODO: move to swap state
            // TODO: select the next notification in swap state
            //  on exit make current = next
            _context.setCurrentNotificationId((notificationId + 1) % _context.getNotifications().size());
            _context.setDirty(true);
        }
        _timeOnNotification += (long) delta;
    }
}
