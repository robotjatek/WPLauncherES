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

    private boolean _dirty = true;
    private final String _packageName;
    private final Deque<StatusBarNotification> _notifications = new ConcurrentLinkedDeque<>();
    private final Label _titleLabel;
    private final Icon _icon;
    private final AbsoluteLayout _layout;
    private final Label _notificationLabel;

    public StaticTileContent(App app) {
        _packageName = app.packageName();
        NotificationListener.subscribe(this);
        _titleLabel = new Label("My App", 48, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
        _icon = new Icon(app.icon());
        _layout = new AbsoluteLayout();
        _notificationLabel = new Label("", 64, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer,
                     Tile tile, Position<Float> position, Size<Integer> size) {
        if (_dirty) {
            // TODO: move this to a command buffer and run before rendering a frame
            var iconSize = Math.min(size.width(), size.height()) / 2;
            _icon.setSize(new Size<>(iconSize, iconSize));
            _titleLabel.setText(tile.getSize().equals(Tile.SMALL) ? "" : tile.title);
            _layout.setBgColor(tile.bgColor);

            // Icon centered
            var iconX = (size.width() - iconSize) / 2f;
            var iconY = (size.height() - iconSize) / 2f;

            // Move icon slightly to left when there are notifications
            if (!_notifications.isEmpty()) {
                iconX -= size.width() / 4f;
                _notificationLabel.setText(_notifications.size()+"");
            }

            _layout.clear();
            _layout.addChild(_icon, new Position<>(iconX, iconY));
            _layout.addChild(_titleLabel, new Position<>(10f, size.height().floatValue() - 60));

            if (!_notifications.isEmpty()) {
                // Notification badge
                var offset = tile.getSize().equals(Tile.WIDE) ? 0 : size.width() / 10f + 30;
                var x = (size.width() - iconSize) / 2f + offset;
                var y = (size.height() - iconSize) / 2f + size.height() / 7f;
                _layout.addChild(_notificationLabel, new Position<>(x, y));
            } else {
                _layout.removeChild(_notificationLabel);
            }

            _dirty = false;
        }

        _layout.draw(delta, projMatrix, viewMatrix, renderer, position, size);
    }

    public void dispose() {
        _layout.dispose(); // Root should dispose all of its children including the nested layouts
        NotificationListener.unsubscribe(this);
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
    public void onChange() {
        // TODO: this now runs on every notification for ALL listeners
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
