package com.robotjatek.wplauncher.TileGrid;

import android.app.Notification;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TextBox.TextBox;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.INotificationChangedListener;
import com.robotjatek.wplauncher.Services.NotificationListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

record InternalNotification(String title, String message) { }

/**
 * Generic notification surface for tiles that don't have dedicated back content
 */
public class NotificationSurface implements ITileContent, INotificationChangedListener {

    private boolean _dirty = true;
    private long _timeOnNotification = 0;
    private int _currentNotificationId = 0;
    private final String _packageName;
    private final List<InternalNotification> _notifications = Collections.synchronizedList(new ArrayList<>());
    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private final Label _titleLabel = new Label("Should not be seen", 56, Typeface.BOLD, Colors.WHITE, Colors.TRANSPARENT);
    private final TextBox _textBox = new TextBox("", 52, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT, 400);

    public NotificationSurface(App app) {
        _packageName = app.packageName();
        NotificationListener.subscribe(this);
        _layout.addChild(_titleLabel, new Position<>(0f, 0f));
        _layout.addChild(_textBox, new Position<>(0f, 0f));
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer,
                     Tile tile, Position<Float> position, Size<Integer> size) {
        if (_dirty) {
            _layout.setBgColor(tile.bgColor);
            if (!_notifications.isEmpty()) {
                var currentNotification = _notifications.get(_currentNotificationId);
                _titleLabel.setText(currentNotification.title());
                _textBox.setText(currentNotification.message());
                var titleX = position.x() + 50;
                var titleY = position.y() + 100;
                var titleHeight = _titleLabel.measure().height();
                _layout.setChildPosition(_titleLabel, new Position<>(titleX, titleY));
                _layout.setChildPosition(_textBox, new Position<>(titleX, titleY + titleHeight));
                var messageMaxWidth = (int)(size.width() - titleX);
                var messageMaxHeight = (int)(size.height() - titleY - titleHeight);
                _titleLabel.setMaxWidth(messageMaxWidth);
                _textBox.setMaxWidth(messageMaxWidth);
                _textBox.setMaxHeight(messageMaxHeight);

            }
            _dirty = false;
        }

        if (_timeOnNotification > 4000 && !_notifications.isEmpty()) {
            _timeOnNotification = 0;
            _currentNotificationId = (_currentNotificationId + 1) % _notifications.size();
            _dirty = true;
        }

        _layout.draw(delta, projMatrix, viewMatrix, renderer, position, size);
        _timeOnNotification += (long) delta;
    }

    @Override
    public void forceRedraw() {
        _dirty = true;
    }

    @Override
    public boolean hasContent() {
        return !_notifications.isEmpty();
    }

    @Override
    public void onChange() {
        var notifications = NotificationListener.getInstance().getNotifications(_packageName);
        _notifications.clear();
        _currentNotificationId = 0;
        for (var n : notifications) {
            var flags = n.getNotification().flags;
            var isGroup = (flags & Notification.FLAG_GROUP_SUMMARY) != 0;
            if (!isGroup) {
                var title = Objects.requireNonNull(n.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE)).toString();
                var text = Objects.requireNonNull(n.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT)).toString();
             //   var messages = n.getNotification().extras.getCharSequence(Notification.EXTRA_MESSAGES);
             //   var summary = n.getNotification().extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
                _notifications.add(new InternalNotification(title, text));
            }
        }
        _dirty = true;
    }

    @Override
    public void dispose() {
        NotificationListener.unsubscribe(this);
        _titleLabel.dispose();
        _textBox.dispose();
    }
}
