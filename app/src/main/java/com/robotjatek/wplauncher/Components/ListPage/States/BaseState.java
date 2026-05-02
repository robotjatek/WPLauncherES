package com.robotjatek.wplauncher.Components.ListPage.States;


import static com.robotjatek.wplauncher.Components.ListPage.ListPage.ITEM_GAP_PX;
import static com.robotjatek.wplauncher.Components.ListPage.ListPage.ITEM_HEIGHT_PX;
import static com.robotjatek.wplauncher.Components.ListPage.ListPage.TOP_MARGIN_PX;

import android.util.Log;

import com.robotjatek.wplauncher.Components.ListPage.ListItem;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.Components.ListPage.ListPage;

import java.util.Optional;

public class BaseState<T> implements IState {

    ListPage<T> _context;

    protected BaseState(ListPage<T> context) {
        _context = context;
    }

    @Override
    public void enter() {
        Log.d(IState.class.toString(), this.getClass().toString());
    }

    @Override
    public void exit() {

    }

    @Override
    public void handleTouchStart(float x, float y) {

    }

    @Override
    public void handleTouchEnd(float x, float y) {

    }

    @Override
    public void handleMove(float x, float y) {

    }

    @Override
    public void update(float delta) {
        _context.getScroll().update(delta);
    }

    protected Optional<ListItem<T>> getItemAt(float y) {
        var adjustedY = y - (_context.getScroll().getScrollOffset() + TOP_MARGIN_PX);
        var index = (int)(adjustedY / (ITEM_HEIGHT_PX + ITEM_GAP_PX));
        if (index >= 0 && index < _context.getItems().size()) {
            return Optional.of(_context.getItems().get(index));
        }

        return Optional.empty();
    }
}
