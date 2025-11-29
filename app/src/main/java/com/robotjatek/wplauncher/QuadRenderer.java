package com.robotjatek.wplauncher;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Has only one set of vertex data, and call draw multiple times with different Model matrices
 */
public class QuadRenderer {

    private Color _color = new Color(1, 0, 0);

    private final int _vboId;
    private final int _iboId;
    private final Shader _shader;

    int positionLoc;
    float[] vertices = {
            // x,    y,    z
            0f,   0f,   0f,
            1f,   0f,   0f,
            1f,   1f,   0f,
            0f,   1f,   0f
    };

    // Two triangles: 0-1-2 and 0-2-3
    private static final short[] indices = {
            0, 1, 2,
            0, 2, 3
    };

   // float[] _modelMatrix = new float[16];
    float[] _mvp = new float[16];

    public QuadRenderer() {
        // Upload vertices here, only one set of vertices, multiple draw calls per objects with different model matrices per objects
        var vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4) // 4 bytes per float
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices).position(0);


        int[] buffers = new int[1];
        GLES20.glGenBuffers(1, buffers, 0);
        _vboId = buffers[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.length * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        var indexBuffer = ByteBuffer
                .allocateDirect(indices.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexBuffer.put(indices).position(0);

        GLES20.glGenBuffers(1, buffers, 0);
        _iboId = buffers[0];
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _iboId);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.length * 2, indexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        _shader = new Shader("", "");
        _shader.use();
        positionLoc = GLES20.glGetAttribLocation(_shader.getId(), "vPosition");
    }

    public void draw(float[] projMatrix, float[] modelMatrix) {

        _shader.use();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _vboId);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _iboId);

        // Set position location to the first location
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glVertexAttribPointer(positionLoc,
                3, GLES20.GL_FLOAT, false,
                3 * 4, 0);

        _shader.setVec4Uniform("color", _color.r(), _color.g(), _color.b(), 1);

        Matrix.multiplyMM(_mvp, 0, projMatrix, 0, modelMatrix, 0);
        _shader.setMat4Uniform("uMVP", _mvp);

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                indices.length,
                GLES20.GL_UNSIGNED_SHORT,
                0
        );

        GLES20.glDisableVertexAttribArray(positionLoc);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

    }

    public void setColor(Color color) {
        _color = color;
    }

}
