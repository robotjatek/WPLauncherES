package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

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

    private final OnChangeListener<AccentColor> _accentColorListener = this::accentChanged; // TODO: make the accent color a property of the theme
    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final StackLayout _layout;
    private final Button _colorPickerBtn;
    private Icon _icon;
    private final SettingsService _settings;
    private Size<Integer> _size = new Size<>(-1, -1);
    private final TextBlock _description;

    public ThemeScreen(IScreenNavigator navigator, SettingsService settings) {
        _navigator = navigator;
        _settings = settings;
        var theme = _settings.getCurrentTheme();
        _layout = new StackLayout(() -> settings.getCurrentTheme().layout());

        var _title = new Label("LAUNCHER SETTINGS", () -> _settings.getCurrentTheme().label(ITheme.TextRole.TITLE));
        _layout.addChild(_title);
        var _subtitle = new Label("theme", () -> settings.getCurrentTheme().label(ITheme.TextRole.SUBTITLE));
        _layout.addChild(_subtitle);

        _layout.addChild(new Spacer(0, 64));

        _description = new TextBlock("You can change your phone's background" +
                " and accent color to match your mood today, this week, or all month",
                () -> _settings.getCurrentTheme().textBlock(), -1);
        _layout.addChild(_description);
        _layout.addChild(new Spacer(0, 48));

        var backgroundLabel = new Label("Background color", () -> settings.getCurrentTheme().label(ITheme.TextRole.DROPDOWN_LABEL));
        _layout.addChild(backgroundLabel);
        var options = _settings.getThemes();
        var _backgroundDropdown = new Dropdown<>(new Size<>(0, 100), options, ITheme::name, _settings::setCurrentTheme, () -> settings.getCurrentTheme().dropdown());
        _backgroundDropdown.setSelected(theme);
        _layout.addChild(_backgroundDropdown);

        _layout.addChild(new Spacer(0, 48));

        var accentLabel = new Label("Accent color", () -> settings.getCurrentTheme().label(ITheme.TextRole.DROPDOWN_LABEL));
        _layout.addChild(accentLabel);
        var color = settings.getAccentColor();
        _icon = new Icon(color.color(), new Size<>(64, 64));
        _colorPickerBtn = new Button(
                color.name(),
                _icon,
                new Size<>(0, 100),
                () -> settings.getCurrentTheme().button(),
                () -> {
                    var colorPickerScreen = new ColorPickerScreen(navigator, settings::getCurrentTheme);
                    colorPickerScreen.subscribe(_accentColorListener);
                    navigator.push(colorPickerScreen);
                });
        _layout.addChild(_colorPickerBtn);

        _settings.subscribeToAccentColorChange(_accentColorListener);
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
            _settings.unsubscribeFromAccentColorChange(_accentColorListener);
            _disposed = true;
        }
    }
}
