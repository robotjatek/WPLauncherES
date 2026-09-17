package com.robotjatek.wplauncher;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.KeyEvent;
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
import com.robotjatek.wplauncher.Services.MediaService;
import com.robotjatek.wplauncher.Services.PermissionService;
import com.robotjatek.wplauncher.Services.ScreenNavigator.ScreenNavigator;
import com.robotjatek.wplauncher.Services.WeatherService.WeatherService;

public class LauncherSurfaceView extends GLSurfaceView implements IUIContext {

    private boolean _disposed = false;
    private final LauncherRenderer _renderer;
    private final GestureDetector _gestureDetector;
    private final IUIContext _uiContext = this;
    private ITextInputHandler _focusedInputHandler = null;
    private CustomInputConnection _currentInputConnection = null;

    public LauncherSurfaceView(Context context, LocationService locationService, PermissionService permissionService, WeatherService weatherService, MediaService mediaService, AppChangeReceiver appChangeReceiver) {
        super(context);
        var navigator = new ScreenNavigator();
        _renderer = new LauncherRenderer(context, locationService, permissionService, weatherService, mediaService, appChangeReceiver, navigator, this);
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
        setEGLConfigChooser(8, 8, 8, 8, 24, 8);
        setRenderer(_renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private int _activePointerId = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        _gestureDetector.onTouchEvent(event);

        var action = event.getActionMasked();
        var index = event.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN -> {
                _activePointerId = event.getPointerId(0);
                var x = event.getX(0);
                var y = event.getY(0);
                queueEvent(() -> _renderer.handleGesture(new DownGesture(x, y, this)));
            }
            case MotionEvent.ACTION_POINTER_DOWN -> {
            }
            case MotionEvent.ACTION_MOVE -> {
                if (_activePointerId != -1) {
                    var pointerIndex = event.findPointerIndex(_activePointerId);
                    if (pointerIndex != -1) {
                        var x = event.getX(pointerIndex);
                        var y = event.getY(pointerIndex);
                        queueEvent(() -> _renderer.handleGesture(new MoveGesture(x, y, this)));
                    }
                }
            }
            case MotionEvent.ACTION_POINTER_UP -> {
                var upId = event.getPointerId(index);
                // send UpGesture for the lifted pointer so targets receive onUp
                var upX = event.getX(index);
                var upY = event.getY(index);
                queueEvent(() -> _renderer.handleGesture(new UpGesture(upX, upY, this)));

                if (upId == _activePointerId) {
                    if (event.getPointerCount() > 1) {
                        var newIndex = index == 0 ? 1 : 0;
                        _activePointerId = event.getPointerId(newIndex);
                        var x = event.getX(newIndex);
                        var y = event.getY(newIndex);
                        queueEvent(() -> _renderer.handleGesture(new MoveGesture(x, y, this)));
                    } else {
                        _activePointerId = -1;
                    }
                }
            }
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                var pointerIndex = event.findPointerIndex(_activePointerId);
                var x = (pointerIndex != -1) ? event.getX(pointerIndex) : event.getX();
                var y = (pointerIndex != -1) ? event.getY(pointerIndex) : event.getY();
                queueEvent(() -> _renderer.handleGesture(new UpGesture(x, y, this)));
                _activePointerId = -1;
            }
        }

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
        _currentInputConnection = new CustomInputConnection(this);
        return _currentInputConnection;
    }

    @Override
    public void requestFocus(ITextInputHandler element) {
        _focusedInputHandler = element;
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

    @Override
    public void onSelectionChanged(ITextInputHandler element) {
        if (_focusedInputHandler == element) {
            var imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            var pos = element.getCursorPosition();
            int cStart = -1, cEnd = -1;
            if (_currentInputConnection != null) {
                cStart = _currentInputConnection.getComposingStart();
                cEnd = _currentInputConnection.getComposingEnd();
            }
            imm.updateSelection(this, pos, pos, cStart, cEnd);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        var handler = getFocusedInputHandler();
        if (handler == null) {
            return super.onKeyDown(keyCode, event);
        }

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            handler.onBackspace();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            cancelFocus(); // TODO: this will not work for multiline inputs
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            handler.decrementCursorPosition();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            handler.incrementCursorPosition();
            return true;
        }

        var uniChar = event.getUnicodeChar();
        if (uniChar != 0) {
            handler.onTextInput(String.valueOf((char) uniChar));
            return true;
        }

        return super.onKeyDown(keyCode, event);
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
        queueEvent(_renderer::onHomePressed);
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
