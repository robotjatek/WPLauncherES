package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.Spacer.Spacer;
import com.robotjatek.wplauncher.Components.TextBlock.TextBlock;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.TileGrid.Position;

public class AboutScreen implements IScreen {

    private boolean _disposed = false;
    private final Context _context;
    private final IScreenNavigator _navigator;
    private Size<Integer> _size = new Size<>(-1, -1);
    private final StackLayout _layout = new StackLayout();
    private final Label _versionLabel = new Label("", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0);
    private final String _githubURL = "https://github.com/robotjatek/WPLauncherES";
    private final String _siteURL = "https://robotjatek.github.io/";
    private final TextBlock _description = new TextBlock("A Windows Phone inspired launcher for Android built from scratch in OpenGL ES",
            48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0, -1);
    private final TextBlock _free = new TextBlock("Free forever. No ads. No paywalls. No data collected. No telemetry.",
            48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0, -1);

    public AboutScreen(IScreenNavigator navigator, Context context, SettingsService settings) {
        _navigator = navigator;
        _context = context;
        _layout.setBgColor(Colors.BLACK);

        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("about", 160, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Spacer(0, 48));
        _layout.addChild(new Label("WP Launcher ES Beta", 60, Typeface.BOLD, Colors.LIGHT_GRAY, 0));
        _layout.addChild(_versionLabel);
        _layout.addChild(new Spacer(0, 48));
        _layout.addChild(_description);
        _layout.addChild(new Spacer(0, 48));
        _layout.addChild(_free);
        _layout.addChild(new Spacer(0, 48));
        _layout.addChild(new Label(_githubURL, 48, Typeface.NORMAL, settings.getAccentColor().color(), 0, -1, () -> launchBrowser(_githubURL)));
        _layout.addChild(new Label(_siteURL, 48, Typeface.NORMAL, settings.getAccentColor().color(), 0, -1, () -> launchBrowser(_siteURL)));
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] view, QuadRenderer renderer) {
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
        _free.setMaxWidth(width);
        var version = "";
        try {
            version = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "";
        }
        _versionLabel.setText("version: " + version);
        _layout.onResize(width, height);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    private void launchBrowser(String url) {
        var intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        _context.startActivity(intent);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
