package com.robotjatek.wplauncher.Components.List.States;


import static com.robotjatek.wplauncher.Components.List.ListView.ITEM_GAP_PX;
import static com.robotjatek.wplauncher.Components.List.ListView.ITEM_HEIGHT_PX;
import static com.robotjatek.wplauncher.Components.List.ListView.TOP_MARGIN_PX;

import com.robotjatek.wplauncher.Components.List.ListItem;
import com.robotjatek.wplauncher.IState;
import com.robotjatek.wplauncher.Components.List.ListView;

import java.util.Optional;

public class BaseState<T> implements IState {

    ListView<T> _context;

    protected BaseState(ListView<T> context) {
        _context = context;
    }

    @Override
    public void enter() {

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
