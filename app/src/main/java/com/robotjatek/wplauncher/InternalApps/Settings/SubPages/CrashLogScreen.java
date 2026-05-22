package com.robotjatek.wplauncher.InternalApps.Settings.SubPages;

import android.content.Context;
import android.graphics.Typeface;
import android.opengl.Matrix;
import android.util.Log;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenu;
import com.robotjatek.wplauncher.Components.ContextMenu.ContextMenuDrawContext;
import com.robotjatek.wplauncher.Components.ContextMenu.MenuOption;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.InternalApps.TextReaderPage;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CrashLogScreen implements IScreen {

    private boolean _disposed = false;
    private final IScreenNavigator _navigator;
    private final Context _context;
    private final StackLayout _layout = new StackLayout();
    private final ListView<File> _crashList;
    private Size<Integer> _size = new Size<>(-1, -1);
    private final float[] _view = new float[16];
    private final Label _titleLabel = new Label("LAUNCHER SETTINGS", 64, Typeface.NORMAL, Colors.WHITE, 0);
    private final Label _subTitleLabel = new Label("crash log", 160, Typeface.NORMAL, Colors.WHITE, 0);
    private final ContextMenuDrawContext<File> _contextMenuDrawContext;

    public CrashLogScreen(IScreenNavigator navigator, Context context) {
        _navigator = navigator;
        _context = context;
        _crashList = new ListView<>(0, 0, 0);
        _contextMenuDrawContext = new ContextMenuDrawContext<>(_crashList);
        _layout.addChild(_titleLabel);
        _layout.addChild(_subTitleLabel);
        _layout.addChild(_crashList);
        _crashList.addItems(createItems());
    }

    @Override
    public void draw(float delta, float[] projMatrix, QuadRenderer renderer) {
        Matrix.setIdentityM(_view, 0);
        _layout.draw(delta, projMatrix, _view, renderer, Position.ZERO, _size);
    }

    @Override
    public void onBackPressed() {
        _navigator.pop();
    }

    @Override
    public void onResize(int width, int height) {
        _size = new Size<>(width, height);
        var itemsHeight = _titleLabel.measure().height() +
                _subTitleLabel.measure().height() + StackLayout.TOP_MARGIN_PX;
        var listHeight = height - itemsHeight;
        _crashList.setSize(new Size<>(width, listHeight));
        _crashList.setMargins(LauncherRenderer.SCREEN_DATA.topInset, LauncherRenderer.SCREEN_DATA.bottomInset);
        _layout.onResize(width, height);
        _crashList.setContextMenu(createContextMenu());
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    private List<File> listFiles() {
        var file = _context.getFilesDir().listFiles();
        if (file == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(file).filter(f -> f.getName().startsWith("crash_")).toList();
    }

    private List<ListItem<File>> createItems() {
        var files = listFiles();
        return files.stream().sorted(Comparator.comparing(File::lastModified)).map(f ->
                new ListItem<>(
                        f.getName(),
                        null,
                        Colors.TRANSPARENT,
                        () -> openFileReaderPage(f), f)).toList();
    }

    private void openFileReaderPage(File file) {
        var page = new TextReaderPage(_navigator, file, (File payload) -> {
            if (payload != null) {
                _crashList.removeItemByPayload(file);
            }
        });
        _navigator.push(page);
    }

    private ContextMenu<File> createContextMenu() {
        var menu = new ContextMenu<>(Position.ZERO, _contextMenuDrawContext);
        var options = List.of(
                new MenuOption<>("Open", this::openFileReaderPage, menu, null),
                new MenuOption<>("Delete", this::deleteFile, menu, null)
        );
        menu.addOptions(options);
        return menu;
    }

    private void deleteFile(File f) {
        if(!f.delete()) {
            Log.e(CrashLogScreen.class.getName(), "Failed to delete file: " + f.getName());
        }
        _crashList.removeItemByPayload(f);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
