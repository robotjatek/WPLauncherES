package com.robotjatek.wplauncher.Services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class NotificationListener extends NotificationListenerService {

    private final Map<String, List<StatusBarNotification>> _notifications = new HashMap<>();
    private final Deque<INotificationChangedListener> _listeners = new ArrayDeque<>();
    private static final Deque<INotificationChangedListener> _pendingListeners = new ConcurrentLinkedDeque<>();
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
        _connected = true;
        _listeners.addAll(_pendingListeners);
        _pendingListeners.clear();
        loadActiveNotifications();
    }

    private void loadActiveNotifications() {
        var notifications = super.getActiveNotifications();
        for (var n : notifications) {
            _notifications.computeIfAbsent(n.getPackageName(), key -> new ArrayList<>()).add(n);
        }
        notifyListeners();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        var packageName = sbn.getPackageName();
        // TODO: wrap SBN into a custom notification
        _notifications.computeIfAbsent(packageName, key -> new ArrayList<>()).add(sbn);
        notifyListeners();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        var packageName = sbn.getPackageName();
        var appNotifications = _notifications.get(packageName);
        if (appNotifications != null) {
            appNotifications.remove(sbn);
        }
        notifyListeners();
    }

    public List<StatusBarNotification> getNotifications(String packageName){
        return _notifications.getOrDefault(packageName, List.of());
    }

    public void removeNotification(String notificationKey) {
        cancelNotification(notificationKey);
    }

    /**
     * Subscribe to notification changes.
     * Subscriptions can be made before the system instantiates the service.
     * Subscribers receive every current notification upon subscription
     * The notification listener is instantiated asynchronously by the system.
     */
    public static void subscribe(INotificationChangedListener listener) {
        if (_instance != null && _instance.isConnected()) {
            // add directly & make it query all current notifications
            _instance._listeners.add(listener);
            listener.onChange();
        } else {
            // Service is not instantiated or connected yet, add listener to pending listeners
            _pendingListeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (var l : _listeners) {
            l.onChange();
        }
    }

    public boolean isConnected() {
        return _connected;
    }

    public static NotificationListener getInstance() {
        return _instance;
    }
}
