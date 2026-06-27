package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.content.Context;
import android.graphics.Typeface;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Checkbox.Checkbox;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.Spacer.Spacer;
import com.robotjatek.wplauncher.Components.TextBlock.TextBlock;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.PermissionService;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.TileGrid.Position;

public class PermissionsScreen implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final PermissionService _permissionService;
    private final StackLayout _layout = new StackLayout();
    private Size<Integer> _size = new Size<>(-1, -1);
    private final TextBlock _description = new TextBlock("You can re-ask for missing permissions from this screen.", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0, -1);
    private final Checkbox _locationCheckbox;
    private final Checkbox _notificationCheckbox;

    public PermissionsScreen(IScreenNavigator navigator, PermissionService permissionService, Context context) {
        _navigator = navigator;
        _permissionService = permissionService;
        _layout.setBgColor(Colors.BLACK);
        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("permissions", 160, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Spacer(0, 32));
        _layout.addChild(_description);
        _layout.addChild(new Spacer(0, 32));

        _locationCheckbox = new Checkbox("location", _permissionService.hasLocationPermission(), (Boolean state) -> {
            if (state) {
                _permissionService.requestLocationPermission();
            }
        }, context);
        _layout.addChild(_locationCheckbox);
        _layout.addChild(new Spacer(0, 32));
        _notificationCheckbox = new Checkbox("notifications", _permissionService.hasNotificationAccess(), (Boolean state) -> {
            if (state) {
                _permissionService.openNotificationSettings();
            }
        }, context);
        _layout.addChild(_notificationCheckbox);
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] view, QuadRenderer renderer) {
        if (_locationCheckbox != null) {
            _locationCheckbox.setState(_permissionService.hasLocationPermission());;
        }
        if (_notificationCheckbox != null) {
            _notificationCheckbox.setState(_permissionService.hasNotificationAccess());
        }
        _layout.draw(delta, projMatrix, view, renderer, Position.ZERO, _size);
    }

    @Override
    public void onBackPressed() {
        _navigator.pop();
    }

    @Override
    public void onResize(int width, int height) {
        _size = new Size<>(width, height);
        _description.setMaxWidth(width);
        _layout.onResize(width, height);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    @Override
    public void dispose() {
        if (_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
