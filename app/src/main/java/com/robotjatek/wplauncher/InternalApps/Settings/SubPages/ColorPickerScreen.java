package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.graphics.drawable.ColorDrawable;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.ListItem;
import com.robotjatek.wplauncher.AppList.ListItemDrawContext;
import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.InternalApps.Components.List.ListView;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.AccentColor;

import java.util.List;

public class ColorPickerScreen implements IScreen {

    private final IScreenNavigator _navigator;
    private final float[] viewMatrix = new float[16];
    private final ListView<AccentColor> _view;

    public ColorPickerScreen(QuadRenderer renderer, IScreenNavigator navigator) {
        _navigator = navigator;
        Matrix.setIdentityM(viewMatrix, 0);
        _view = new ListView<>(renderer);
        _view.addItems(createItems(_view.getDrawContext()));
    }

    private List<ListItem<AccentColor>> createItems(ListItemDrawContext<AccentColor, ListView<AccentColor>> drawContext) {
        return Colors.ACCENT_COLORS.stream().map(i -> {
           return new ListItem<>(i.name(), new ColorDrawable(i.color()), drawContext, () -> {}, i);
            // TODO: onTap runnable
        }).toList();
    }

    @Override
    public void draw(float delta, float[] projMatrix) {
        _view.draw(delta, projMatrix, viewMatrix);
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
    public void onTouchStart(float x, float y) {
        _view.touchStart(x, y);
    }

    @Override
    public void onTouchEnd(float x, float y) {
        _view.touchEnd(x, y);
    }

    @Override
    public void onTouchMove(float x, float y) {
        _view.touchMove(x, y);
    }

    @Override
    public void dispose() {
        _view.dispose();
    }
}
