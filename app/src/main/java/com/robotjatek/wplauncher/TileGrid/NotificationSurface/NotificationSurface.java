package com.robotjatek.wplauncher.TileGrid.NotificationSurface;

import android.app.Notification;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.INotificationChangedListener;
import com.robotjatek.wplauncher.Services.NotificationListener;
import com.robotjatek.wplauncher.TileGrid.ITileContent;
import com.robotjatek.wplauncher.TileGrid.NotificationSurface.States.IdleState;
import com.robotjatek.wplauncher.TileGrid.Position;
import com.robotjatek.wplauncher.TileGrid.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic notification surface for tiles that don't have dedicated back content
 */
public class NotificationSurface implements ITileContent, INotificationChangedListener {

    private boolean _disposed = false;
    private boolean _dirty = true;
    private final String _packageName;
    private final List<InternalNotification> _notifications = Collections.synchronizedList(new ArrayList<>());
    private IState _state = IDLE_STATE();
    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    private final AbsoluteLayout _layout = new AbsoluteLayout();
    private NotificationElement _currentNotification = new NotificationElement();
    private int _currentNotificationId = 0;
    private Tile _tile;
    private Size<Integer> _lastSize = new Size<>(0, 0);

    public NotificationSurface(App app) {
        _packageName = app.packageName();
        NotificationListener.subscribe(_packageName, this);
        _layout.addChild(_currentNotification, Position.ZERO);
    }

    public Tile getParent() {
        return _tile;
    }

    @Override
    public void setParent(Tile tile) {
        _tile = tile;
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix, QuadRenderer renderer,
                     Position<Float> position, Size<Integer> size) {
        _lastSize = size;
        _state.update(delta);
        _layout.draw(delta, projMatrix, viewMatrix, renderer, position, size);
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
    public void onNotificationsChanged() {
        var notifications = NotificationListener.getInstance().getNotifications(_packageName);
        _notifications.clear();
        _currentNotificationId = 0;
        for (var n : notifications) {
            var flags = n.getNotification().flags;
            var isGroup = (flags & Notification.FLAG_GROUP_SUMMARY) != 0;
            if (!isGroup) {
                var title = n.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
                var text = n.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);
                if (title == null && text == null) continue; // skip notifications without content;
             //   var messages = n.getNotification().extras.getCharSequence(Notification.EXTRA_MESSAGES);
             //   var summary = n.getNotification().extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
                _notifications.add(
                        new InternalNotification(
                                title != null ? title.toString() : "",
                                text != null ? text.toString() : ""));
            }
        }
        changeState(IDLE_STATE());
        _dirty = true;
    }

    public boolean isDirty() {
        return _dirty;
    }

    public void setDirty(boolean dirty) {
        _dirty = dirty;
    }

    public NotificationElement getCurrentNotification() {
        return _currentNotification;
    }

    public int getCurrentNotificationId() {
        return _currentNotificationId;
    }

    public void setCurrentNotificationId(int id) {
        _currentNotificationId = id;
    }

    public List<InternalNotification> getNotifications() {
        return _notifications;
    }

    public Size<Integer> getLastSize() {
        return _lastSize;
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            NotificationListener.unsubscribe(_packageName, this);
            _layout.dispose();
            _currentNotification.dispose();
            _disposed = true;
        }
    }
}
