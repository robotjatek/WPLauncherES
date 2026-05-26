package com.robotjatek.wplauncher.Components.ListView.States;

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

    public Optional<ListItem<T>> getItemAt(float x, float y) {
        var _children = _context.getVisibleItems();
        var _drawContext = _context.getItemDrawContext();
        for (var child : _children) {
            var left = _drawContext.xOf(child);
            var top = _drawContext.yOf(child) + _context.getScroll().getScrollOffset() + _context.getPadding();
            var right = left + _drawContext.widthOf(child);
            var bottom = top + _drawContext.heightOf(child);
            if (x >= left && x <= right && y >= top && y <= bottom) {
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }
}
