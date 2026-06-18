package com.robotjatek.wplauncher.InternalApps;

import android.graphics.Typeface;
import android.opengl.Matrix;
import android.util.Log;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.ScrollView.ScrollView;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.TextBlock.TextBlock;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.Services.ScreenNavigator.IScreenNavigator;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TextReaderPage implements IScreen {

    private boolean _disposed = false;
    public final IScreenNavigator _navigator;
    private final float[] _modelMatrix = new float[16];
    private Size<Integer> _size = new Size<>(-1, -1);
    private final StackLayout _layout = new StackLayout();
    private final TextBlock _textbox;
    private final Button _deleteButton; // delete the file and close the page
    private final ScrollView _scrollView;

    private final File _file;
    private boolean _dirty = true;
    private final Consumer<File> _onClose;

    public TextReaderPage(IScreenNavigator navigator, File file, Consumer<File> onClose) {
        _navigator = navigator;
        _file = file;
        _onClose = onClose;
        var content = readFileContent(file);
        _deleteButton = new Button("Delete log", null, new Size<>(0, 100), this::deleteFileAndExit);
        _textbox = new TextBlock(content, 38, Typeface.NORMAL, Colors.WHITE, Colors.TRANSPARENT, -1);
        _scrollView = new ScrollView(_textbox, 0, LauncherRenderer.SCREEN_DATA.bottomInset);
        _layout.addChild(_deleteButton);
        _layout.addChild(_scrollView);

    }

    @Override
    public void draw(float delta, float[] projMatrix, QuadRenderer renderer) {
        if (_dirty) {
            _dirty = false;
        }

        Matrix.setIdentityM(_modelMatrix, 0);
        _layout.draw(delta, projMatrix, _modelMatrix, renderer, Position.ZERO, _size);
    }

    @Override
    public void onBackPressed() {
        _navigator.pop();
        _onClose.accept(null);
    }

    @Override
    public void onResize(int width, int height) {
        _size = new Size<>(width, height);
        var itemsHeight = _deleteButton.measure().height() + StackLayout.TOP_MARGIN_PX;
        _textbox.setMaxWidth(width);
        _scrollView.setSize(new Size<>(width, height - itemsHeight - LauncherRenderer.SCREEN_DATA.topInset));
        _layout.onResize(width, height);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture);
    }

    private String readFileContent(File file) {
        try (var reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "";
        }
    }

    private void deleteFileAndExit() {
        if (!_file.delete()) {
            Log.e(TextReaderPage.class.getName(), "Failed to delete file: " + _file.getName());
        }
        _navigator.pop();
        _onClose.accept(_file);
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
