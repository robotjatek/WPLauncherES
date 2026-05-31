package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.robotjatek.wplauncher.Components.InputBox.ITextInputHandler;
import com.robotjatek.wplauncher.Gestures.DownGesture;
import com.robotjatek.wplauncher.Gestures.LongPressGesture;
import com.robotjatek.wplauncher.Gestures.MoveGesture;
import com.robotjatek.wplauncher.Gestures.ScrollGesture;
import com.robotjatek.wplauncher.Gestures.TapGesture;
import com.robotjatek.wplauncher.Gestures.UpGesture;
import com.robotjatek.wplauncher.Services.AppChangeReceiver;
import com.robotjatek.wplauncher.Services.LocationService;

public class LauncherSurfaceView extends GLSurfaceView implements IUIContext {

    private boolean _disposed = false;
    private final LauncherRenderer _renderer;
    private final GestureDetector _gestureDetector;
    private final IUIContext _uiContext = this;
    private ITextInputHandler _focusedInputHandler = null;

    public LauncherSurfaceView(Context context, LocationService locationService, AppChangeReceiver appChangeReceiver) {
        super(context);
        _renderer = new LauncherRenderer(context, locationService, appChangeReceiver);
        _gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
        {
           @Override
           public boolean onSingleTapUp(@NonNull MotionEvent e) {
               // extract the x and y coordinates before queueing the event, to avoid potential issues with the MotionEvent being recycled before the event is processed
               var x = e.getX();
               var y = e.getY();
               queueEvent(() -> _renderer.handleGesture(new TapGesture(x, y, _uiContext)));
               return true;
           }

           @Override
           public void onLongPress(@NonNull MotionEvent e) {
               var x = e.getX();
               var y = e.getY();
               queueEvent(() -> _renderer.handleGesture(new LongPressGesture(x, y, _uiContext)));
           }

           @Override
           public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
               var x = e2.getX();
               var y = e2.getY();
               queueEvent(() -> _renderer.handleGesture(new ScrollGesture(x, y, distanceX, distanceY, _uiContext)));
               return true;
           }

           @Override
           public boolean onDown(@NonNull MotionEvent e) {
               var x = e.getX();
               var y = e.getY();
               queueEvent(() -> _renderer.handleGesture(new DownGesture(x, y, _uiContext)));
               return true;
           }

            @Override
           public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                                  float velocityX, float velocityY) {
               // TODO: renderer->handleGesture(new scrollgesture(x, y))
               return true;
           }
        });

        setFocusable(true);
        setFocusableInTouchMode(true);
        setEGLContextClientVersion(3);
        setRenderer(_renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        var x = event.getX();
        var y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP -> queueEvent(() -> _renderer.handleGesture(new UpGesture(x, y, this)));
            case MotionEvent.ACTION_MOVE -> queueEvent(() -> _renderer.handleGesture(new MoveGesture(x, y, this)));
        }

        _gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new CustomInputConnection(this);
    }

    @Override
    public void requestFocus(ITextInputHandler element) {
        _focusedInputHandler = element;
        element.onFocus();
        post(() -> {
            requestFocus();
            var imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.restartInput(this);
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    @Override
    public void cancelFocus() {
        if (_focusedInputHandler != null) {
            _focusedInputHandler.onFocusLost();
        }
        _focusedInputHandler = null;
        post(() -> {
            clearFocus();
            var imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        });
    }

    public void onBackPressed() {
        if (_focusedInputHandler != null) {
            cancelFocus();
            return;
        }
        queueEvent(_renderer::onBackPressed);
    }

    public void onHomePressed() {
        if (_focusedInputHandler != null) {
            cancelFocus();
            return;
        }
        _renderer.onHomePressed();
    }

    public ITextInputHandler getFocusedInputHandler() {
        return _focusedInputHandler;
    }

    public LauncherRenderer getRenderer() {
        return _renderer;
    }

    public void dispose() {
        if (!_disposed) {
            _renderer.dispose();
            _disposed = true;
        }
    }
}
