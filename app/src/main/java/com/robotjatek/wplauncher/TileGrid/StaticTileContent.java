package com.robotjatek.wplauncher.TileGrid;

import android.app.Notification;
import android.graphics.Typeface;
import android.service.notification.StatusBarNotification;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.INotificationChangedListener;
import com.robotjatek.wplauncher.Services.NotificationListener;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class StaticTileContent implements ITileContent, INotificationChangedListener {

    private boolean _disposed = false;
    private boolean _dirty = true;
    private final String _packageName;
    private final Deque<StatusBarNotification> _notifications = new ConcurrentLinkedDeque<>();
    private final Label _titleLabel;
    private final Icon _icon;
    private final AbsoluteLayout _layout;
    private final Label _notificationLabel;
    private boolean _isNotificationLabelAdded = false;
    private final static Size<Integer> ICON_SIZE = new Size<>(256, 256);
    private App _lastApp;

    public StaticTileContent(App app) {
        _packageName = app.packageName();
        NotificationListener.subscribe(_packageName, this);
        _lastApp = app;
        _layout = new AbsoluteLayout();
        _titleLabel = new Label(app.name(), 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _notificationLabel = new Label("", 64, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _icon = new Icon(app.icon(), ICON_SIZE);
        _layout.addChild(_icon, Position.ZERO);
        _layout.addChild(_titleLabel, Position.ZERO);
    }

    private void updateApp(App app) {
        if (_lastApp != app) {
            _icon.setIconDrawable(app.icon());
            _lastApp = app;
            _dirty = true;
        }
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer,
                     Tile tile, Position<Float> position, Size<Integer> size) {
        if (_dirty) {
            // TODO: move this to a command buffer and run before rendering a frame
            updateApp(tile.getApp());
            var padding = size.height() * 0.035f;
            var iconSize = Math.min(size.width(), size.height()) / 2;
            _icon.setSize(new Size<>(iconSize, iconSize));
            var titleText = tile.getSize().equals(Tile.SMALL) ? "" : tile.getApp().name();
            if (!titleText.equals(_titleLabel.getText())) {
                _titleLabel.setText(titleText);
            }
            _titleLabel.setMaxWidth(size.width() - padding * 2);
            _layout.setBgColor(tile.bgColor);

            // Icon centered
            var iconX = (size.width() - iconSize) / 2f;
            var iconY = (size.height() - iconSize) / 2f;

            // Move icon slightly to left when there are notifications
            if (!_notifications.isEmpty()) {
                iconX -= size.width() / 4f;
            }

            _layout.setChildPosition(_icon, new Position<>(iconX, iconY));
            var labelHeight = _titleLabel.measure().height();
            _layout.setChildPosition(_titleLabel, new Position<>(padding, size.height() - labelHeight - padding / 2));

            if (!_notifications.isEmpty()) {
                // Notification badge
                var offset = size.width() * 0.025f;
                var notificationTextSize = tile.getSize().equals(Tile.SMALL) ? 48 : 144;
                if (_notificationLabel.getTextSize() != notificationTextSize) {
                    _notificationLabel.setTextSize(notificationTextSize);
                }

                var notificationText = String.valueOf(_notifications.size());
                if (!notificationText.equals(_notificationLabel.getText())) {
                    _notificationLabel.setText(notificationText);
                }

                var x = iconX + iconSize + offset;
                var y = position.y() + iconSize - _notificationLabel.measure().height() / 2f;

                if (!_isNotificationLabelAdded) {
                    _layout.addChild(_notificationLabel, new Position<>(x, y));
                    _isNotificationLabelAdded = true;
                } else {
                    _layout.setChildPosition(_notificationLabel, new Position<>(x, y));
                }
            } else {
                if (_isNotificationLabelAdded) {
                    _layout.removeChild(_notificationLabel);
                    _isNotificationLabelAdded = false;
                }
            }

            _dirty = false;
        }

        _layout.draw(delta, projMatrix, viewMatrix, renderer, position, size);
    }

    public void dispose() {
        if (!_disposed) {
            _layout.dispose(); // Root should dispose all of its children including the nested layouts,
            _icon.dispose();
            _titleLabel.dispose();
            _notificationLabel.dispose(); // dynamically removed items are not disposed automatically, making sure its disposed (double dispose is safe)
            NotificationListener.unsubscribe(_packageName, this);
            _disposed = true;
        }
    }

    @Override
    public void forceRedraw() {
        _dirty = true;
    }

    @Override
    public boolean hasContent() {
        return true;
    }

    @Override
    public void onNotificationsChanged() {
        var notifications = NotificationListener.getInstance().getNotifications(_packageName);
        _notifications.clear();
        for (var n : notifications) {
            var flags = n.getNotification().flags;
            var isGroup = (flags & Notification.FLAG_GROUP_SUMMARY) != 0;
            if (!isGroup) {
                _notifications.add(n);
            }
        }
        _dirty = true;
    }
}
