package com.robotjatek.wplauncher;

import android.opengl.Matrix;

// TODO: a scrollingot kiszervezni egy külön (base?)osztályba
// TODO: meg a view alapú render logicot is...
public class AppList implements Page {

    private final float[] pageMatrix = new float[16]; // scroll position transformation
    private final float[] testModel = new float[16];;
    private final QuadRenderer testRenderer = new QuadRenderer(); // TODO: this is just to show some test data as content

    public AppList() {}

    @Override
    public void draw(float delta, float[] projMatrix, float[] viewMatrix) {
        Matrix.setIdentityM(testModel, 0);
        Matrix.scaleM(testModel, 0, 300, 150, 1);

        float[] vm = new float[16];
        Matrix.multiplyMM(vm, 0, viewMatrix, 0, testModel, 0);

        testRenderer.draw(projMatrix, vm);
    }

    @Override
    public void touchMove(float y) {
        // TODO: handle scrolling
    }

    @Override
    public void touchStart(float y) {
        // TODO: handle scrolling
    }
}
