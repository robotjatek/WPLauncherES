package com.robotjatek.wplauncher;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Has only one set of vertex data, and call draw multiple times with different Model matrices
 */
public class QuadRenderer {

    private final int _vboId;
    private final int _iboId;
    private final int _texCoordVBOId;
    private final Shader _shader;

    int _positionLoc;
    int _texCoordLoc;

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
        var buffers = new int[1];

        // Upload vertices here, only one set of vertices, multiple draw calls per objects with different model matrices per objects
        var vertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4) // 4 bytes per float
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(VERTICES).position(0);

        GLES20.glGenBuffers(1, buffers, 0);
        _vboId = buffers[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, VERTICES.length * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        var texCoordBuffer = ByteBuffer
                .allocateDirect(TEX_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texCoordBuffer.put(TEX_COORDS).position(0);

        GLES20.glGenBuffers(1, buffers, 0);
        _texCoordVBOId = buffers[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _texCoordVBOId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, TEX_COORDS.length * 4, texCoordBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        var indexBuffer = ByteBuffer
                .allocateDirect(INDICES.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexBuffer.put(INDICES).position(0);
        GLES20.glGenBuffers(1, buffers, 0);
        _iboId = buffers[0];
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _iboId);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, INDICES.length * 2, indexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        _shader = shader;
        _shader.use();
        _positionLoc = GLES20.glGetAttribLocation(_shader.getId(), "vPosition");
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
        GLES20.glEnableVertexAttribArray(_positionLoc);
        GLES20.glVertexAttribPointer(_positionLoc, 3, GLES20.GL_FLOAT, false, 3 * 4, 0);

        // texcoords
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _texCoordVBOId);
        GLES20.glEnableVertexAttribArray(_texCoordLoc);
        GLES20.glVertexAttribPointer(_texCoordLoc, 2, GLES20.GL_FLOAT, false, 2 * 4, 0);

        Matrix.multiplyMM(_mvp, 0, projMatrix, 0, modelMatrix, 0);
        _shader.setMat4Uniform("uMVP", _mvp);

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                INDICES.length,
                GLES20.GL_UNSIGNED_SHORT,
                0);

        GLES20.glDisableVertexAttribArray(_positionLoc);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void dispose() {
    }
}
