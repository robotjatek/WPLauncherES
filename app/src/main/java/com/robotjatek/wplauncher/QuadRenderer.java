package com.robotjatek.wplauncher;

import android.opengl.GLES32;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Has only one set of vertex data, and call draw multiple times with different Model matrices
 */
public class QuadRenderer {

    private boolean _disposed = false;
    private final int _vaoId;
    private final int _vboId;
    private final int _iboId;
    private final int _texCoordVBOId;
    private final Shader _shader;
    private static final int POSITION_LOCATION = 0;
    private static final int TEX_COORD_LOCATION = 1;

    private static final float[] VERTICES = {
            // x,    y,    z
            0f,   0f,   0f,
            1f,   0f,   0f,
            1f,   1f,   0f,
            0f,   1f,   0f
    };

    // Two triangles: 0-1-2 and 0-2-3
    private static final short[] INDICES = {
            0, 1, 2,
            0, 2, 3
    };

    private static final float[] TEX_COORDS = {
            0f,  0f,  // bottom-left
            1f,  0f,  // bottom-right
            1f,  1f,  // top-right
            0f,  1f   // top-left
    };

    float[] _mvp = new float[16];

    public QuadRenderer(Shader shader) {
        _shader = shader;
        _shader.use();

        var buffers = new int[1];
        var arrays = new int[1];

        GLES32.glGenVertexArrays(1, arrays, 0);
        _vaoId = arrays[0];
        GLES32.glBindVertexArray(_vaoId);

        // Upload vertices here, only one set of vertices, multiple draw calls per objects with different model matrices per objects
        var vertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4) // 4 bytes per float
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(VERTICES).position(0);

        GLES32.glGenBuffers(1, buffers, 0);
        _vboId = buffers[0];
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, _vboId);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, VERTICES.length * 4, vertexBuffer, GLES32.GL_STATIC_DRAW);

        GLES32.glEnableVertexAttribArray(POSITION_LOCATION);
        GLES32.glVertexAttribPointer(POSITION_LOCATION, 3, GLES32.GL_FLOAT, false, 3 * 4, 0);

        var texCoordBuffer = ByteBuffer
                .allocateDirect(TEX_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texCoordBuffer.put(TEX_COORDS).position(0);

        GLES32.glGenBuffers(1, buffers, 0);
        _texCoordVBOId = buffers[0];
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, _texCoordVBOId);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, TEX_COORDS.length * 4, texCoordBuffer, GLES32.GL_STATIC_DRAW);

        GLES32.glEnableVertexAttribArray(TEX_COORD_LOCATION);
        GLES32.glVertexAttribPointer(TEX_COORD_LOCATION, 2, GLES32.GL_FLOAT, false, 2 * 4, 0);

        var indexBuffer = ByteBuffer
                .allocateDirect(INDICES.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexBuffer.put(INDICES).position(0);
        GLES32.glGenBuffers(1, buffers, 0);
        _iboId = buffers[0];
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, _iboId);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, INDICES.length * 2, indexBuffer, GLES32.GL_STATIC_DRAW);

        GLES32.glBindVertexArray(0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
    }

    public void draw(float[] projMatrix, float[] modelMatrix, int textureId) {
        _shader.use();

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureId);
        _shader.setIntUniform("uTexture", 0);

        GLES32.glEnable(GLES32.GL_BLEND);
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);

        Matrix.multiplyMM(_mvp, 0, projMatrix, 0, modelMatrix, 0);
        _shader.setMat4Uniform("uMVP", _mvp);

        GLES32.glBindVertexArray(_vaoId);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, INDICES.length, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
    }

    public void dispose() {
        if (!_disposed) {
            GLES32.glDeleteVertexArrays(1, new int[]{_vaoId}, 0);
            GLES32.glDeleteBuffers(3, new int[] { _vboId, _iboId, _texCoordVBOId }, 0);
            _disposed = true;
        }
    }
}
