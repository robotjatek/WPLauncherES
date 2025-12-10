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
    private final int _texCoordVBOId;
    private final Shader _shader;

    int positionLoc;
    int _texCoordLoc;

    private static final float[] vertices = {
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

    private static final float[] _texCoords = {
            0f,  0f,  // bottom-left
            1f,  0f,  // bottom-right
            1f,  1f,  // top-right
            0f,  1f   // top-left
    };

    float[] _mvp = new float[16];

    public QuadRenderer() {
        var buffers = new int[1];

        // Upload vertices here, only one set of vertices, multiple draw calls per objects with different model matrices per objects
        var vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4) // 4 bytes per float
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        GLES20.glGenBuffers(1, buffers, 0);
        _vboId = buffers[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.length * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        var texCoordBuffer = ByteBuffer
                .allocateDirect(_texCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texCoordBuffer.put(_texCoords).position(0);

        GLES20.glGenBuffers(1, buffers, 0);
        _texCoordVBOId = buffers[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _texCoordVBOId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, _texCoords.length * 4, texCoordBuffer, GLES20.GL_STATIC_DRAW);
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
        _texCoordLoc = GLES20.glGetAttribLocation(_shader.getId(), "aTexCoord");
    }

    public void draw(float[] projMatrix, float[] modelMatrix, int textureId) {

        _shader.use();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        _shader.setIntUniform("uTexture", 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _iboId);

        // position
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _vboId);
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glVertexAttribPointer(positionLoc, 3, GLES20.GL_FLOAT, false, 3 * 4, 0);

        // texcoords
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _texCoordVBOId);
        GLES20.glEnableVertexAttribArray(_texCoordLoc);
        GLES20.glVertexAttribPointer(_texCoordLoc, 2, GLES20.GL_FLOAT, false, 2 * 4, 0);

        _shader.setVec4Uniform("color", _color.r(), _color.g(), _color.b(), 1);

        Matrix.multiplyMM(_mvp, 0, projMatrix, 0, modelMatrix, 0);
        _shader.setMat4Uniform("uMVP", _mvp);

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                indices.length,
                GLES20.GL_UNSIGNED_SHORT,
                0);

        GLES20.glDisableVertexAttribArray(positionLoc);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
