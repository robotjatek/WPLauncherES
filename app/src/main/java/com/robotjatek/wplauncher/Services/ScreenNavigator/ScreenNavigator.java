package com.robotjatek.wplauncher.Services.ScreenNavigator;

import android.opengl.Matrix;

import androidx.annotation.NonNull;

import com.robotjatek.wplauncher.Colors;
import com.robotjatek.wplauncher.Components.Layouts.AbsoluteLayout.AbsoluteLayout;
import com.robotjatek.wplauncher.Components.Layouts.StackLayout.StackLayout;
import com.robotjatek.wplauncher.Components.Modal.IModal;
import com.robotjatek.wplauncher.Components.Size;
import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.ScreenNavigator.States.ClosingModalState;
import com.robotjatek.wplauncher.Services.ScreenNavigator.States.ClosingScreenState;
import com.robotjatek.wplauncher.Services.ScreenNavigator.States.IdleState;
import com.robotjatek.wplauncher.Services.ScreenNavigator.States.OpeningModalState;
import com.robotjatek.wplauncher.Services.ScreenNavigator.States.OpeningScreenState;
import com.robotjatek.wplauncher.TileGrid.Position;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScreenNavigator implements IScreenNavigator, IOverlay {

    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public IState OPENING_MODAL_STATE(IModal modal) {
        return new OpeningModalState(this, modal);
    }

    public IState CLOSING_MODAL_STATE() {
        return new ClosingModalState(this);
    }

    public IState OPENING_SCREEN_STATE(IScreen screen) {
        return new OpeningScreenState(this, screen);
    }

    public IState CLOSING_SCREEN_STATE(IScreen screen) {
        return new ClosingScreenState(this, screen);
    }

    private IState _state = IDLE_STATE();
    private final Queue<Runnable> _commands = new ConcurrentLinkedQueue<>();
    private IModal _modal;
    private IScreen _animatedScreen;
    private float _animatedScreenTranslation;
    private final Deque<IScreen> _navigationStack = new ConcurrentLinkedDeque<>();
    private int _width = -1;
    private int _height = -1;
    private final float[] _model = new float[16];
    private final StackLayout _fullscreen = new StackLayout();
    private final AbsoluteLayout _overlay = new AbsoluteLayout();

    public ScreenNavigator() {
        _fullscreen.setBgColor(Colors.BLACK);
    }

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public void setAnimatedScreen(IScreen screen) {
        _animatedScreen = screen;
    }

    public void setAnimatedScreenTranslation(float translation) {
        _animatedScreenTranslation = translation;
    }

    public void draw(float delta, float[] proj, QuadRenderer renderer) {
        executeCommands();
        _state.update(delta);

        var size = new Size<>(_width, _height);
        if (!_navigationStack.isEmpty()) {
            Matrix.setIdentityM(_model, 0);
            _navigationStack.getFirst().draw(delta, proj, _model, renderer);
        }

        if (_animatedScreen != null) {
            Matrix.setIdentityM(_model, 0);
            Matrix.translateM(_model, 0, _model, 0, _animatedScreenTranslation, 0, 0);

            renderer.pushLayers(100);
            _fullscreen.draw(delta, proj, _model, renderer, new Position<>(0f, -(float)LauncherRenderer.SCREEN_DATA.topInset), size);
            _animatedScreen.draw(delta, proj, _model, renderer);
            renderer.popLayers(100);
        }

        if (_modal != null) {
            Matrix.setIdentityM(_model, 0);
            Matrix.scaleM(_model, 0, _width, _height, 1);
            renderer.pushLayers(200);
            renderer.drawFlat(proj, _model, 0x88050505);
            _modal.draw(delta, proj, renderer);
            renderer.popLayers(200);
        }

        if (_overlay.hasContent()) {
            Matrix.setIdentityM(_model, 0);
            renderer.pushLayers(300);
            _overlay.draw(delta, proj, _model, renderer, Position.ZERO, size);
            renderer.popLayers(300);
        }
    }

    @Override
    public void push(@NonNull IScreen screen) {
        changeState(OPENING_SCREEN_STATE(screen));
    }

    public void pushToNavigationStack(IScreen screen) {
        _commands.add(() -> {
            _navigationStack.push(screen);
            _animatedScreen = null;
        });
    }

    @Override
    public void pop() {
        var screen = _navigationStack.getFirst();
        changeState(CLOSING_SCREEN_STATE(screen));
    }

    public void popFromNavigationStack() {
        _commands.add(_navigationStack::pop);
    }

    public void init(IScreen screen) {
        while (!_navigationStack.isEmpty()) {
            _navigationStack.pop().dispose();
        }
        _navigationStack.push(screen);
    }

    public void handleGesture(Gesture gesture) {
        if (_overlay.handleGesture(gesture)) {
            return;
        }
        _state.handleGesture(gesture);
    }

    @Override
    public void onBackPressed() {
        // if modal is open dismiss it
        if (_modal != null) {
            dismissModal();
            return;
        }

        _navigationStack.getFirst().onBackPressed();
    }

    public void onHomePressed() {
        _commands.add(() -> {
            if (_modal != null) {
                dismissModal();
            }

            if (!_navigationStack.isEmpty()) {
                while (_navigationStack.size() > 1) {
                    _navigationStack.pop().dispose();
                }
                var startScreen = _navigationStack.getLast();
                startScreen.onBackPressed();
            }
        });
    }

    public void onResize(int width, int height) {
        _width = width;
        _height = height;
        _navigationStack.forEach(s -> s.onResize(width, height));
        _overlay.onResize(width, height);
    }

    @Override
    public void openModal(IModal modal) {
        changeState(OPENING_MODAL_STATE(modal));
    }

    @Override
    public void dismissModal() {
        if (_modal == null || _state instanceof ClosingModalState) {
            return;
        }
        changeState(CLOSING_MODAL_STATE());
    }

    public void disposeModal() {
        if (_modal != null) {
            var toDispose = _modal;
            _modal = null;
            _commands.add(toDispose::dispose);
        }
    }

    public IModal getModal() {
        return _modal;
    }

    public void setModal(IModal modal) {
        _modal = modal;
    }

    public int getHeight() {
        return _height;
    }

    public int getWidth() {
        return _width;
    }

    public IScreen getCurrentScreen() {
        return _navigationStack.getFirst();
    }

    @Override
    public void setAdornerAt(UIElement element, Position<Float> position) {
        _overlay.setChildPosition(element, position);
    }

    private void executeCommands() {
        Runnable command;
        while ((command = _commands.poll()) != null) {
            command.run();
        }
    }

    public void dispose() {
        _navigationStack.forEach(IScreen::dispose);
        if (_modal != null) {
            _modal.dispose();
            _modal = null;
            _fullscreen.dispose();
        }
        _overlay.dispose();
        _navigationStack.clear();
    }
}
