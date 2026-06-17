package com.robotjatek.wplauncher.Components.Modal;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Button.Button;
import com.robotjatek.wplauncher.Components.Label.Label;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.Spacer.Spacer;
import com.robotjatek.wplauncher.Components.TextBlock.TextBlock;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.TileGrid.Position;

public class Modal implements IModal {

    private boolean _disposed = false;
    private final StackLayout _layout = new StackLayout();
    private final StackLayout _contentLayout = new StackLayout();
    private final StackLayout _buttonLayout = new StackLayout();
    private final Spacer _buttonSpacer = new Spacer(-1, -1);
    private final Label _titleLabel = new Label("", 72, Typeface.NORMAL, Colors.WHITE, 0);
    private final TextBlock _messageBlock = new TextBlock("", 48, Typeface.NORMAL, Colors.LIGHT_GRAY, 0, 0);
    private final float[] _model = new float[16];

    public Modal(String title, String message, Runnable onOk, Runnable onDismiss) {
        _layout.setBgColor(Colors.CONTEXT_MENU_GRAY);
        _layout.addChild(_contentLayout);
        _contentLayout.addChild(new Spacer(0, LauncherRenderer.SCREEN_DATA.topInset));
        _titleLabel.setText(title);
        _contentLayout.addChild(_titleLabel);
        _messageBlock.setText(message);
        _contentLayout.addChild(_messageBlock);
        _layout.addChild(_buttonSpacer);
        _buttonLayout.addChild(new Button("OK", null, onOk));
        _buttonLayout.addChild(new Button("Cancel", null, onDismiss));
        _layout.addChild(_buttonLayout); // TODO: buttons on the bottom
    }

    @Override
    public void draw(float delta, float[] projMatrix, QuadRenderer renderer) {
        Matrix.setIdentityM(_model, 0);
        Matrix.translateM(_model, 0, _model, 0, 0, -LauncherRenderer.SCREEN_DATA.topInset, 0);
        _layout.draw(delta, projMatrix, _model, renderer, Position.ZERO, new Size<>(_layout.getWidth(), _layout.getHeight()));
    }

    @Override
    public void onResize(int width, int height) {
        _messageBlock.setMaxWidth(width);

        var topPadding = LauncherRenderer.SCREEN_DATA.topInset;
        var titleHeight = _titleLabel.measure().height();
        var buttonAreaHeight = _buttonLayout.measure().height();

        var reserved = topPadding + titleHeight + buttonAreaHeight;
        _messageBlock.setMaxHeight(Math.max(0, height - reserved));

        _contentLayout.onResize(width, height);
        _buttonLayout.onResize(width, height);

        var totalContentHeight = _contentLayout.measure().height();
        var finalButtonHeight = _buttonLayout.measure().height();
        var spacerHeight = Math.max(0, height - totalContentHeight - finalButtonHeight);
        _buttonSpacer.setSize(width, spacerHeight);
        _layout.onResize(width, height);
    }

    @Override
    public boolean handleGesture(Gesture gesture) {
        return _layout.handleGesture(gesture.copyWithOffset(0, LauncherRenderer.SCREEN_DATA.topInset)); // we are ignoring the inset as we translated the modal to the top of the screen
    }

    @Override
    public void dispose() {
        if (!_disposed) {
            _layout.dispose();
            _disposed = true;
        }
    }
}
