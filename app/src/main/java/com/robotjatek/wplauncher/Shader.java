package com.robotjatek.wplauncher;

import android.opengl.GLES20;
import android.util.Log;

public class Shader {

    private final int _shaderProgram;

    // TODO: multiple shaders per type
    // TODO: load file via path
    public Shader(String vertexPath, String fragmentPath) {
        var vertexShaderCode =
                        "attribute vec4 vPosition;" +
                        "attribute vec2 aTexCoord;" +
                        "varying vec2 vTexCoord;" +
                        "uniform mat4 uMVP;" +
                        "void main() {" +
                        "  gl_Position = uMVP * vPosition;" +
                        "  vTexCoord = aTexCoord;" +
                        "}";

        String fragmentShaderCode =
                        "precision mediump float;" +
                        "uniform sampler2D uTexture;" +
                        "varying vec2 vTexCoord;" +
                        "void main() {" +
                        "  vec4 texColor = texture2D(uTexture, vTexCoord);" +
                        "  gl_FragColor = texColor;" +
                        "}";

        var vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        var fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        _shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(_shaderProgram, vertexShader);
        GLES20.glAttachShader(_shaderProgram, fragmentShader);
        GLES20.glLinkProgram(_shaderProgram);
        GLES20.glDetachShader(_shaderProgram, vertexShader);
        GLES20.glDetachShader(_shaderProgram, fragmentShader);
    }

    public void use() {
        GLES20.glUseProgram(_shaderProgram);
    }

    public void delete() {
        GLES20.glDeleteShader(_shaderProgram);
    }

    public int getId() {
        return _shaderProgram;
    }

    // TODO: cache location
    public void setVec4Uniform(String name, float x, float y, float z, float w) {
        this.use();
        var location = GLES20.glGetUniformLocation(_shaderProgram, name);
        GLES20.glUniform4f(location, x, y, z, w);
    }

    public void setMat4Uniform(String name, float[] matrix) {
        this.use();
        var location = GLES20.glGetUniformLocation(_shaderProgram, name);
        GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
    }

    public void setIntUniform(String name, int value) {
        this.use();
        var location = GLES20.glGetUniformLocation(_shaderProgram, name);
        GLES20.glUniform1i(location, value);
    }

    private static int loadShader(int type, String shaderCode){
        var shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("GL", "Could not compile shader " + type + ": " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }
}
