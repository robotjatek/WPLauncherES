package com.robotjatek.wplauncher.AppList.States;

import static com.robotjatek.wplauncher.AppList.AppList.ITEM_GAP_PX;
import static com.robotjatek.wplauncher.AppList.AppList.ITEM_HEIGHT_PX;

import android.util.Log;

import com.robotjatek.wplauncher.AppList.App;
import com.robotjatek.wplauncher.AppList.AppList;
import com.robotjatek.wplauncher.AppList.ListItem;
import com.robotjatek.wplauncher.IGestureState;

import java.util.Optional;

public abstract class BaseState implements IGestureState {

    protected final AppList _context;

    public BaseState(AppList context) {
        _context = context;
    }

    @Override
    public void enter() {
        Log.d(IGestureState.class.toString(), this.getClass().toString());
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

    protected Optional<ListItem<App>> getItemAt(float y) {
        var adjustedY = y - (_context.getScroll().getScrollOffset() + AppList.TOP_MARGIN_PX);
        var index = (int)(adjustedY / (ITEM_HEIGHT_PX + ITEM_GAP_PX));
        if (index >= 0 && index < _context.getItems().size()) {
            return Optional.of(_context.getItems().get(index));
        }

        return Optional.empty();
    }
}
