package com.robotjatek.wplauncher.Services;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationListener extends NotificationListenerService {

    private static final Object _lock = new Object();
    private final Map<String, List<StatusBarNotification>> _notifications = new ConcurrentHashMap<>();
    private final Map<String, List<INotificationChangedListener>> _listeners = new ConcurrentHashMap<>();
    private static final Map<String, List<INotificationChangedListener>> _pendingListeners = new ConcurrentHashMap<>();
    private boolean _connected = false;
    private static NotificationListener _instance;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        synchronized (_lock) {
            _connected = true;

            // The notification listener is instantiated asynchronously by the system, but subscriptions can be initiated before the service is ready
            // On connect we move all pending listeners to the real listener list
            for (var entry : _pendingListeners.entrySet()) {
                _listeners.computeIfAbsent(entry.getKey(), k -> new CopyOnWriteArrayList<>()).addAll(entry.getValue());
            }
            _pendingListeners.clear();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        var isGroupNotification = (sbn.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) != 0;
        if (!isGroupNotification) {
            var packageName = sbn.getPackageName();
            var appNotifications = _notifications.computeIfAbsent(packageName, key -> new ArrayList<>());

            // An already existing notification was updated:
            appNotifications.removeIf(n -> Objects.equals(n.getKey(), sbn.getKey()));

            appNotifications.add(sbn);
            notifyListeners(packageName);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        var packageName = sbn.getPackageName();
        var appNotifications = _notifications.get(packageName);

        // If it is a group notification remove the whole group from the list
        // NOTE: as of now this is kind of a dead code, because I don't store group notifications
        var isGroupNotification = (sbn.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) != 0;
        if (isGroupNotification) {
            if (appNotifications != null) {
                appNotifications.removeIf(n -> Objects.equals(n.getGroupKey(), sbn.getGroupKey()));
            }
        } else { // Remove only that notification that was dismissed
            if (appNotifications != null) {
                appNotifications.removeIf(n -> Objects.equals(n.getKey(), sbn.getKey()));
            }
        }

        notifyListeners(packageName);
    }

    public List<StatusBarNotification> getNotifications(String packageName){
        return _notifications.getOrDefault(packageName, List.of());
    }

    /**
     * Subscribe to notification changes for a specific package.
     * Subscriptions can be made before the system instantiates the service.
     * Subscribers receive the current notification state upon subscription.
     * The notification listener is instantiated asynchronously by the system.
     */
    public static void subscribe(String packageName, INotificationChangedListener listener) {
        synchronized (_lock) {
            if (_instance != null && _instance.isConnected()) {
                _instance._listeners.computeIfAbsent(packageName, k -> new CopyOnWriteArrayList<>()).add(listener);
                listener.onNotificationsChanged(); // Notify immediately with current state
            } else {
                _pendingListeners.computeIfAbsent(packageName, k -> new CopyOnWriteArrayList<>()).add(listener);
            }
        }
    }

    public static void unsubscribe(String packageName, INotificationChangedListener listener) {
        synchronized (_lock) {
            List<INotificationChangedListener> listeners;
            if (_instance != null && _instance.isConnected()) {
                listeners = _instance._listeners.get(packageName);
            } else {
                listeners = _pendingListeners.get(packageName);
            }
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    private void notifyListeners(String packageName) {
        var listeners = _listeners.get(packageName);
        if (listeners != null) {
            for (var l : listeners) {
                l.onNotificationsChanged();
            }
        }
    }

    public boolean isConnected() {
        return _connected;
    }

    public static NotificationListener getInstance() {
        return _instance;
    }
}