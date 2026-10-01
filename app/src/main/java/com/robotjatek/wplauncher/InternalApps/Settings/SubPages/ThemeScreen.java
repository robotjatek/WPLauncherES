package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.graphics.Typeface;
import android.util.Log;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.Components.Dropdown.Dropdown;
import com.robotjatek.wplauncher.Components.Icon.Icon;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.Spacer.Spacer;
import com.robotjatek.wplauncher.Components.TextBlock.TextBlock;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.InternalApps.Settings.OnChangeListener;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.AccentColor;
import com.robotjatek.wplauncher.Services.SettingsService;
import com.robotjatek.wplauncher.Theme.ITheme;
import com.robotjatek.wplauncher.TileGrid.Position;

public class ThemeScreen implements IScreen {

    private final OnChangeListener<AccentColor> _accentColorListener = this::accentChanged;
    private final OnChangeListener<ITheme> _themeListener = this::themeChanged;
    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final StackLayout _layout;
    private final Button _colorPickerBtn;
    private final Dropdown<ITheme> _backgroundDropdown;
    private Icon _icon;
    private final SettingsService _settings;
    private Size<Integer> _size = new Size<>(-1, -1);
    private final TextBlock _description = new TextBlock("You can change your phone's background" +
            " and accent color to match your mood today, this week, or all month",
            48, Typeface.NORMAL, Colors.LIGHT_GRAY, Colors.TRANSPARENT, -1);

    public ThemeScreen(IScreenNavigator navigator, SettingsService settings) {
        _navigator = navigator;
        _settings = settings;
        _settings.subscribeToAccentColorChange(_accentColorListener);
        _settings.subscribeToThemeChange(_themeListener);
        _layout = new StackLayout();
        _layout.setBgColor(Colors.BLACK);

        _layout.addChild(new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0));
        _layout.addChild(new Label("theme", 160, Typeface.NORMAL, Colors.WHITE, 0));

        _layout.addChild(new Spacer(0, 64));
        _layout.addChild(_description);
        _layout.addChild(new Spacer(0, 48));

        var selectedContent = new Label("", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0); // TODO: remove when theme change is implemented
        _layout.addChild(selectedContent);

        _layout.addChild(new Label("Background color", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0));
        var options = _settings.getThemes();
        _backgroundDropdown = new Dropdown<>(new Size<>(0, 100), options, ITheme::name,
                (selected) -> {
                    selectedContent.setText(selected.name());
                    _settings.setCurrentTheme(selected);
                });
        _layout.addChild(_backgroundDropdown);

        _layout.addChild(new Spacer(0, 48));

        _layout.addChild(new Label("Accent color", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0));
        var color = settings.getAccentColor();
        _icon = new Icon(color.color(), new Size<>(64, 64));
        _colorPickerBtn = new Button(
                color.name(),
                _icon,
                new Size<>(0, 100),
                () -> {
                    var colorPickerScreen = new ColorPickerScreen(navigator);
                    colorPickerScreen.subscribe(_accentColorListener);
                    navigator.push(colorPickerScreen);
                });
        _layout.addChild(_colorPickerBtn);
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
        _description.setMaxWidth(width - StackLayout.DEFAULT_PADDING);
        _layout.onResize(width, height);
    }

    private void accentChanged(AccentColor changed) {
        _settings.setAccentColor(changed);
        _icon.dispose();
        _colorPickerBtn.setText(changed.name());
        _icon = new Icon(changed.color(), new Size<>(64, 64));
        _colorPickerBtn.setIcon(_icon);
    }

    private void themeChanged(ITheme theme) {
        Log.d("", theme.name());
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _icon.dispose();
            _description.dispose();
            _settings.unsubscribeFromThemeChange(_themeListener);
            _settings.unsubscribeFromAccentColorChange(_accentColorListener);
            _disposed = true;
        }
    }
}
