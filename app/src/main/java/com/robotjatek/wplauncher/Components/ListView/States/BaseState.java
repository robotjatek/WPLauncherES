package com.robotjatek.wplauncher.Components.ListView.States;

import static com.robotjatek.wplauncher.Components.ListView.ListView.ITEM_GAP_PX;
import static com.robotjatek.wplauncher.Components.ListView.ListView.ITEM_HEIGHT_PX;

import android.util.Log;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.IState;

import java.util.Optional;

public class BaseState<T> implements IState {

    protected ListView<T> _context;

    protected BaseState(ListView<T> context) {
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
    public void update(float delta) {
        _context.getScroll().update(delta);
    }

    protected Optional<ListItem<T>> getItemAt(float y) {
        var adjustedY = y - (_context.getScroll().getScrollOffset() + _context.getPadding());
        var index = (int)(adjustedY / (ITEM_HEIGHT_PX + ITEM_GAP_PX));
        if (index >= 0 && index < _context.getItems().size()) {
            return Optional.of(_context.getItems().get(index));
        }

        return Optional.empty();
    }
}
