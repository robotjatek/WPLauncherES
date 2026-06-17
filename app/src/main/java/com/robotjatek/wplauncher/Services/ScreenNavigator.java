package com.robotjatek.wplauncher.Services;

import androidx.annotation.NonNull;

import com.robotjatek.wplauncher.Gestures.Gesture;
import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.QuadRenderer;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ScreenNavigator implements IScreenNavigator {

    private final Deque<IScreen> _navigationStack = new ConcurrentLinkedDeque<>();
    private int _width = -1;
    private int _height = -1;

    public void draw(float delta, float[] proj, QuadRenderer renderer) {
        _navigationStack.getFirst().draw(delta, proj, renderer); // TODO: animation
        // TODO: modal
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
        if (!_navigationStack.isEmpty()) {
            _navigationStack.getFirst().handleGesture(gesture);
        }
    }

    public void onBackPressed() {
        // TODO: if modal is open dismiss it
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

    public void dispose() {
        _navigationStack.forEach(IScreen::dispose);
        _navigationStack.clear();
    }
}
