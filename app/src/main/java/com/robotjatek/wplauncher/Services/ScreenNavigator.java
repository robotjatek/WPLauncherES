package com.robotjatek.wplauncher.Services;

import android.opengl.Matrix;

import androidx.annotation.NonNull;

import com.robotjatek.wplauncher.Components.Modal.IModal;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.LauncherRenderer;
import com.robotjatek.wplauncher.QuadRenderer;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScreenNavigator implements IScreenNavigator {

    private final Queue<Runnable> _commands = new ConcurrentLinkedQueue<>();
    private IModal _modal;
    private final Deque<IScreen> _navigationStack = new ConcurrentLinkedDeque<>();
    private int _width = -1;
    private int _height = -1;
    private final float[] _model = new float[16];

    public void draw(float delta, float[] proj, QuadRenderer renderer) {
        executeCommands();
        _navigationStack.getFirst().draw(delta, proj, renderer); // TODO: animated screen change
        if (_modal != null) {
            Matrix.setIdentityM(_model, 0);
            Matrix.scaleM(_model, 0, _width, _height, 1);
            _modal.onResize(_width, _height / 3);
            renderer.pushLayer();
            renderer.drawFlat(proj, _model, 0xee050505);
            _modal.draw(delta, proj, renderer);
            renderer.popLayer();
        }
    }

    @Override
    public void push(@NonNull IScreen screen) {
        _commands.add(() -> {
            screen.onResize(_width, _height);
            _navigationStack.push(screen);
        });
    }

    @Override
    public void pop() {
        _commands.add(() -> _navigationStack.pop().dispose());
    }

    public void init(IScreen screen) {
        while (!_navigationStack.isEmpty()) {
            _navigationStack.pop().dispose();
        }
        _navigationStack.push(screen);
    }

    public void handleGesture(Gesture gesture) {
        var modalTop = -LauncherRenderer.SCREEN_DATA.topInset;
        var modalHeight = _height / 3f;
        var modalBottom = modalTop + modalHeight;

        if (_modal != null) {
            if (gesture.getY() >= modalTop && gesture.getY() <= modalBottom) {
                _modal.handleGesture(gesture);
            } else {
                _modal.dispose();
                _modal = null;
            }
            return;
        }


        if (!_navigationStack.isEmpty()) {
            _navigationStack.getFirst().handleGesture(gesture);
        }
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
    }

    @Override
    public void openModal(IModal modal) {
        _modal = modal;
    }

    @Override
    public void dismissModal() {
        _commands.add(() -> {
            if (_modal != null) {
                _modal.dispose();
                _modal = null;
            }
        });
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
        }
        _navigationStack.clear();
    }
}
