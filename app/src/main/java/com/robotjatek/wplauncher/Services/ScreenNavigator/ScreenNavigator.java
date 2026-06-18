package com.robotjatek.wplauncher.Services.ScreenNavigator;

import android.opengl.Matrix;

import androidx.annotation.NonNull;

import com.robotjatek.wplauncher.Components.Modal.IModal;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Services.ScreenNavigator.States.ClosingModalState;
import com.robotjatek.wplauncher.Services.ScreenNavigator.States.IdleState;
import com.robotjatek.wplauncher.Services.ScreenNavigator.States.OpeningModalState;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScreenNavigator implements IScreenNavigator {

    public IState IDLE_STATE() {
        return new IdleState(this);
    }

    public IState OPENING_MODAL_STATE(IModal modal) {
        return new OpeningModalState(this, modal);
    }

    public IState CLOSING_MODAL_STATE() {
        return new ClosingModalState(this);
    }

    private IState _state = IDLE_STATE();

    private final Queue<Runnable> _commands = new ConcurrentLinkedQueue<>();
    private IModal _modal;
    private final Deque<IScreen> _navigationStack = new ConcurrentLinkedDeque<>();
    private int _width = -1;
    private int _height = -1;
    private final float[] _model = new float[16];

    public void changeState(IState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    public void draw(float delta, float[] proj, QuadRenderer renderer) {
        executeCommands();
        _state.update(delta);

        _navigationStack.getFirst().draw(delta, proj, renderer); // TODO: animated screen change
        if (_modal != null) {
            Matrix.setIdentityM(_model, 0);
            Matrix.scaleM(_model, 0, _width, _height, 1);
            renderer.pushLayer();
            renderer.drawFlat(proj, _model, 0x88050505);
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
    }

    @Override
    public void openModal(IModal modal) {
        modal.onResize(_width, _height / 3);
        changeState(OPENING_MODAL_STATE(modal));
        _modal = modal;
    }

    @Override
    public void dismissModal() {
        changeState(CLOSING_MODAL_STATE());
    }

    public void disposeModal() {
        _commands.add(() -> {
            if (_modal != null) {
                _modal.dispose();
                _modal = null;
            }
        });
    }

    public IModal getModal() {
        return _modal;
    }

    public int getHeight() {
        return _height;
    }

    public IScreen getCurrentScreen() {
        return _navigationStack.getFirst();
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
