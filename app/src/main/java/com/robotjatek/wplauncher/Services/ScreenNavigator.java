package com.robotjatek.wplauncher.Services;

import android.opengl.Matrix;

import androidx.annotation.NonNull;

import com.robotjatek.wplauncher.Components.Modal.IModal;
import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.QuadRenderer;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ScreenNavigator implements IScreenNavigator {

    private IModal _modal;
    private final Deque<IScreen> _navigationStack = new ConcurrentLinkedDeque<>();
    private int _width = -1;
    private int _height = -1;
    private final float[] _model = new float[16];

    public void draw(float delta, float[] proj, QuadRenderer renderer) {
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
        screen.onResize(_width, _height);
        _navigationStack.push(screen);
    }

    @Override
    public void pop() {
        _navigationStack.pop().dispose();
    }

    public void init(IScreen screen) {
        while (!_navigationStack.isEmpty()) {
            _navigationStack.pop().dispose();
        }
        _navigationStack.push(screen);
    }

    public void handleGesture(Gesture gesture) {
        // TODO: modal gesture routing
        // TODO: tapped on modal or not?
        //  onModal -> route gesture to modal
        //  else -> route gesture to screen
        if (!_navigationStack.isEmpty()) {
            _navigationStack.getFirst().handleGesture(gesture);
        }
        if (_modal != null) {
            _modal.handleGesture(gesture);
        }
    }

    @Override
    public void onBackPressed() {
        // if modal is open dismiss it
        if (_modal != null) {
            _modal.dispose();
            _modal = null;
            return;
        }

        _navigationStack.getFirst().onBackPressed();
    }

    public void onHomePressed() {
        if (!_navigationStack.isEmpty()) {
            while (_navigationStack.size() > 1) {
                _navigationStack.pop().dispose();
            }
            var startScreen = _navigationStack.getLast();
            startScreen.onBackPressed();
        }
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

    public void dispose() {
        _navigationStack.forEach(IScreen::dispose);
        if (_modal != null) {
            _modal.dispose();
            _modal = null;
        }
        _navigationStack.clear();
    }
}
