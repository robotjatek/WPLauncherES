package com.robotjatek.wplauncher.InternalApps;

import android.graphics.Typeface;
import android.opengl.Matrix;

import com.robotjatek.wplauncher.IScreen;
import com.robotjatek.wplauncher.IScreenNavigator;
import com.robotjatek.wplauncher.QuadRenderer;
import com.robotjatek.wplauncher.Shader;
import com.robotjatek.wplauncher.TileUtil;
import com.robotjatek.wplauncher.VerticalAlign;

/**
 * TODO: settings page should be completely STATELESS as its a singleton!
 */
public class Settings implements IScreen {

    private final IScreenNavigator _navigator;
    private final Shader _shader = new Shader("","");
    private final QuadRenderer _renderer = new QuadRenderer(_shader);

    private final int _texId ;

    public Settings(IScreenNavigator navigator) {
        _navigator = navigator;
        _texId = TileUtil.createTextTexture("Hello", 200, 400, 48, Typeface.NORMAL, 0xffffffff, 0x0, VerticalAlign.CENTER);
    }

    @Override
    public void draw(float delta, float[] projMatrix) {
        var modelM = new float[16];
        Matrix.setIdentityM(modelM, 0);
        Matrix.scaleM(modelM, 0, modelM, 0, 200, 400, 0);
        _renderer.draw(projMatrix, modelM, _texId);
    }

    @Override
    public void onBackPressed() {
        _navigator.pop();
    }

    @Override
    public void onResize(int width, int height) {

    }

    @Override
    public void onTouchStart(float x, float y) {

    }

    @Override
    public void onTouchEnd(float x, float y) {

    }

    @Override
    public void onTouchMove(float x, float y) {

    }

    @Override
    public void dispose() {
        _renderer.dispose();
        _shader.delete();
    }
}
