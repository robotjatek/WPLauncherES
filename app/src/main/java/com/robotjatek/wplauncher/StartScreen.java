package com.robotjatek.wplauncher;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * The start screen has two pages: the TileGrid and the Application list
 * The pages are rendered side-by-side
 * Both page can be scrolled independently
 * Swiping left and right changes between the pages

 * <p>NOTE: This essentially became a carousel view with 2 hardcoded pages</p>
 */
public class StartScreen {

    private int _screenWidth;
    private int _screenHeight;
    private float _touchStartX;
    private float _touchStartY;
    private float totalDeltaX = 0;
    private float totalDeltaY = 0;
    private boolean isSwiping = false;
    private boolean isScrolling = false;

    private float _x;
    private float _y;

    private final TileGrid _tileGrid = new TileGrid();
    private final AppList _appList = new AppList();

    private int _currentPage = 0;
    private float _pageOffset = 0;
    private final List<Page> _pages = new ArrayList<>(List.of(_tileGrid, _appList));

    float[] projMatrix = new float[16];
    float[] pageMatrix = new float[16]; // stores the page translation relative to each other

    public StartScreen() {
    }

    public void draw(float delta) {
        for (var i = 0; i < _pages.size(); i++) {
            var xOffset = (i - _currentPage) * _screenWidth + _pageOffset;
            Matrix.setIdentityM(pageMatrix, 0);
            Matrix.translateM(pageMatrix, 0, xOffset, 0, 0);

            _pages.get(i).draw(delta, projMatrix, pageMatrix);
        }
    }

    public void onTouchMove(float x, float y) {
        float dx = x - _x;

        _x = x;
        _y = y;

        // total movement relative to the gesture start
        totalDeltaX = x - _touchStartX;
        totalDeltaY = y - _touchStartY;

        if (!isSwiping && !isScrolling) {
            if (Math.abs(totalDeltaX) > 30 && Math.abs(totalDeltaX) > Math.abs(totalDeltaY)) {
                isSwiping = true;
            }
            else if (Math.abs(totalDeltaY) > 10 && Math.abs(totalDeltaY) > Math.abs(totalDeltaX)) {
                isScrolling = true;
            }
        }

        if (isScrolling) {
            _pages.get(_currentPage).touchMove(y);
        }

        if (isSwiping) {
            _pageOffset += dx;

            if (_currentPage == 0) {
                _pageOffset = Math.min(_pageOffset, 0);
            } else if (_currentPage == _pages.size() - 1) {
                _pageOffset = Math.max(_pageOffset, 0);
            }
        }
    }

    public void onTouchStart(float x, float y) {
        _touchStartX = x;
        _touchStartY = y;

        totalDeltaX = 0;
        totalDeltaY = 0;

        isSwiping = false;
        isScrolling = false;

        _x = x;
        _y = y;

        _pages.get(_currentPage).touchStart(x, y);
    }

    public void onTouchEnd(float x, float y) {
        if (isSwiping) {
            float threshold = _screenWidth / 3f;
            if (_pageOffset > threshold && _currentPage > 0) {
                _currentPage--;
            }
            else if (_pageOffset < -threshold && _currentPage < _pages.size() - 1) {
                _currentPage++;
            }

            _pageOffset = 0;

            isSwiping = false;
            isScrolling = false;
            totalDeltaX = 0;
            totalDeltaY = 0;
        } else {
            _pages.get(_currentPage).touchEnd(x, y);
        }
    }

    public void onResize(int width, int height) {
        _screenHeight = height;
        _screenWidth = width;
        Matrix.orthoM(projMatrix, 0, 0, width, height, 0, -1, 1);
        _tileGrid.onSizeChanged(width, height);
    }
}
