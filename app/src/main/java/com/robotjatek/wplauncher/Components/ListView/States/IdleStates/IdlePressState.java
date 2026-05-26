package com.robotjatek.wplauncher.Components.ListView.States.IdleStates;

import com.robotjatek.wplauncher.Components.ListView.ListItem;
import com.robotjatek.wplauncher.Components.ListView.ListView;
import com.robotjatek.wplauncher.Components.ListView.States.IdleState;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;

/**
 * Represents press state on a list item.
 * The item remains visually unchanged for a short time. Once the delay time elapses,
 * {@link ListItem#onPress} is called and the item enters its pressed visual state.
 * Releasing the finger transitions to {@link IdleReleaseState}.
 * <p>
 * Moving beyond the movement threshold, scrolling, or performing a long press
 * cancels the press. If the tile was already in its pressed visual state, that
 * state is first reverted before transitioning away.
 * </p>
 */
public class IdlePressState<T> extends IdleBaseState<T> {

    private static final float PRESS_DELAY_MS = 100f;
    private static final float MOVE_DELAY_PX = 16f;
    private static final float MOVE_DELAY_SQUARED = MOVE_DELAY_PX * MOVE_DELAY_PX;
    private final ListItem<T> _item;
    private final float _downX;
    private final float _downY;
    private float _pressDelayRemainingMs;
    private boolean _shrunk;

    public IdlePressState(ListView<T> context, IdleState<T> idle, ListItem<T> item, float downX, float downY) {
        super(context, idle);
        _item = item;
        _downX = downX;
        _downY = downY;
    }

    @Override
    public void enter() {
        _pressDelayRemainingMs = PRESS_DELAY_MS;
        _shrunk = false;
    }

    @Override
    public void update(float delta) {
        if (_shrunk) {
            return; // if already shrunk do nothing
        }

        // wait until the press delay timer runs out
        _pressDelayRemainingMs -= delta;
        if (_pressDelayRemainingMs <= 0f) {
            _item.onPress(); // shrinks the list item
            _shrunk = true;
        }
    }

    @Override
    public boolean handleMove(MoveGesture gesture) {
        // abort press on a movement
        var dx = gesture.getX() - _downX;
        var dy = gesture.getY() - _downY;
        if (dx * dx + dy * dy > MOVE_DELAY_SQUARED) {
            abortPress(_item, _shrunk);
            _idle.changeState(_idle.IDLE_READY_STATE());
        }
        return true;
    }

    @Override
    public boolean handleScroll(ScrollGesture gesture) {
        // abort pressing and start scrolling
        abortPress(_item, _shrunk);
        _context.changeState(_context.SCROLL_STATE(gesture.getY()));
        return _context.handleGesture(gesture);
    }

    @Override
    public boolean handleLongPress(LongPressGesture gesture) {
        if (_context.hasContextMenu()) {
            abortPress(_item, _shrunk);
            _context.changeState(_context.CONTEXT_MENU_STATE(gesture.getX(), gesture.getY()));
            return true;
        }

        // No menu, but we've "handled" the long press by doing nothing.
        // We stay in IdlePressState so that handleUp will still fire the tap.
        return true;
    }

    @Override
    public boolean handleUp(UpGesture gesture) {
        _idle.changeState(_idle.RELEASE_STATE(_item, _shrunk));
        return true;
    }
}
