package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.graphics.drawable.ColorDrawable;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.Components.ListPage.ListPage;
import com.robotjatek.wplauncher.InternalApps.Settings.OnChangeListener;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.AccentColor;
import com.robotjatek.wplauncher.Theme.ITheme;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ColorPickerScreen implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final ListPage<AccentColor> _view;
    private final List<OnChangeListener<AccentColor>> _changeListeners = new ArrayList<>();

    private final Supplier<ITheme> _themeSupplier;

    public ColorPickerScreen(IScreenNavigator navigator, Supplier<ITheme> themeSupplier) {
        _navigator = navigator;
        _themeSupplier = themeSupplier;
        _view = new ListPage<>(themeSupplier);
        _view.addItems(createItems());
    }

    private List<ListItem<AccentColor>> createItems() {
        return Colors.ACCENT_COLORS.stream().map(accentColor ->
                new ListItem<>(accentColor.name(),
                        new ColorDrawable(accentColor.color()),
                        Colors.TRANSPARENT,
                        () -> selectColor(accentColor), accentColor,
                        _themeSupplier)
                ).toList();
    }

    public void subscribe(OnChangeListener<AccentColor> listener) {
        _changeListeners.add(listener);
    }

    private void selectColor(AccentColor color) {
        _changeListeners.forEach(l -> l.changed(color));
        _navigator.pop();
    }

    @Override
    public void draw(float delta, float[] projMatrix, float[] view, QuadRenderer renderer) {
        _view.draw(delta, projMatrix, view, renderer);
    }

    @Override
    public void onBackPressed() {
        _navigator.pop();
    }

    @Override
    public void onResize(int width, int height) {
        _view.onSizeChanged(width, height);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _view.handleGesture(gesture);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _view.dispose();
            _disposed = true;
        }
    }
}
