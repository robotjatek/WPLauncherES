package com.robotjatek.wplauncher.StartPage;

import android.content.Context;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.AppList.AppList;
import com.robotjatek.wplauncher.Page;
import com.robotjatek.wplauncher.StartPage.States.ChildControlState;
import com.robotjatek.wplauncher.StartPage.States.IGestureState;
import com.robotjatek.wplauncher.StartPage.States.IdleState;
import com.robotjatek.wplauncher.StartPage.States.LongPressState;
import com.robotjatek.wplauncher.StartPage.States.ScrollState;
import com.robotjatek.wplauncher.StartPage.States.SwipingState;
import com.robotjatek.wplauncher.StartPage.States.TappedState;
import com.robotjatek.wplauncher.StartPage.States.TouchingState;
import com.robotjatek.wplauncher.TileGrid.TileGrid;
import com.robotjatek.wplauncher.TileService;

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

    private IGestureState _state;

    public IGestureState IDLE_STATE() {
        return new IdleState(this);
    }

    public IGestureState TOUCHING_STATE(float x, float y) {
        return new TouchingState(this, x, y);
    }

    public IGestureState TAPPED_STATE(float x, float y) {
        return new TappedState(this, x, y);
    }

    public IGestureState LONG_PRESS_STATE(float x, float y) {
        return new LongPressState(this, x, y);
    }

    public IGestureState CHILD_CONTROL_STATE() {
        return new ChildControlState(this);
    }

    public IGestureState SCROLL_STATE() {
        return new ScrollState(this);
    }

    public IGestureState SWIPE_STATE(float initialX) {
        return new SwipingState(this, initialX);
    }

    public void changeState(IGestureState state) {
        _state.exit();
        _state = state;
        _state.enter();
    }

    private int _screenWidth;
    private int _screenHeight;

    private final TileGrid _tileGrid;
    private final AppList _appList;

    private int _currentPage = 0;
    private float _pageOffset = 0;
    private final List<Page> _pages;
    private final TileService _tileService;

    float[] projMatrix = new float[16];
    float[] pageMatrix = new float[16]; // stores the page translation relative to each other

    public StartScreen(Context context) {
        _state = IDLE_STATE();
        _tileService = new TileService(context);
        _tileGrid = new TileGrid(_tileService, context);
        _appList = new AppList(context, _tileService);
        _pages = new ArrayList<>(List.of(_tileGrid, _appList));
    }

    public void draw(float delta) {
        _tileService.executeCommands();
        for (var i = 0; i < _pages.size(); i++) {
            var xOffset = (i - _currentPage) * _screenWidth + _pageOffset;
            Matrix.setIdentityM(pageMatrix, 0);
            Matrix.translateM(pageMatrix, 0, xOffset, 0, 0);

            _pages.get(i).draw(delta, projMatrix, pageMatrix);
        }
        _state.update(delta);
    }

    public void onTouchMove(float x, float y) {
        _state.handleMove(x, y);
    }

    public void onTouchStart(float x, float y) {
        _state.handleTouchStart(x, y);
    }

    public void onTouchEnd(float x, float y) {
        _state.handleTouchEnd(x, y);
    }

    public void onResize(int width, int height) {
        _screenHeight = height;
        _screenWidth = width;
        Matrix.orthoM(projMatrix, 0, 0, width, height, 0, -1, 1);
        _tileGrid.onSizeChanged(width, height);
        _appList.onSizeChanged(width, height);
    }

    public Page getCurrentPage() {
        return _pages.get(_currentPage);
    }

    public boolean isChildrenCatchingGestures() {
        return _pages.stream().anyMatch(Page::isCatchingGestures);
    }

    public float getPageOffset() {
        return _pageOffset;
    }

    public void setPageOffset(float offset) {
        var left = (_currentPage *_screenWidth) - offset;
        var right = left + _screenWidth;
        if (left < 0 || right > _pages.size() * _screenWidth) {
            return;
        }

        _pageOffset = offset;
    }

    public float getScreenWidth() {
        return _screenWidth;
    }

    public void nextPage() {
        if (_currentPage + 1 >= _pages.size()) {
            return;
        }
        _currentPage++;
    }

    public void previousPage() {
        if (_currentPage - 1 < 0) {
            return;
        }
        _currentPage--;
    }

    public void dispose() {
        _tileService.persistTiles();
        _pages.forEach(Page::dispose);
        _tileService.dispose();
    }
}
