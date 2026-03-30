package com.robotjatek.wplauncher;

import android.opengl.GLES32;
import android.util.Log;

public class Shader {

    private final int _shaderProgram;

    // TODO: multiple shaders per type
    // TODO: load file via path
    public Shader(String vertexPath, String fragmentPath) {
        var vertexShaderCode =
                "#version 300 es\n" +
                        "layout(location = 0) in vec4 vPosition;" +
                        "layout(location = 1) in vec2 aTexCoord;" +
                        "out vec2 vTexCoord;" +
                        "uniform mat4 uMVP;" +
                        "void main() {" +
                        "  gl_Position = uMVP * vPosition;" +
                        "  vTexCoord = aTexCoord;" +
                        "}";

        var fragmentShaderCode =
                "#version 300 es\n" +
                        "precision mediump float;" +
                        "uniform sampler2D uTexture;" +
                        "in vec2 vTexCoord;" +
                        "out vec4 color;" +
                        "void main() {" +
                        "  vec4 texColor = texture(uTexture, vTexCoord);" +
                        "  color = texColor;" +
                        "}";

        var vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        var fragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);

        _shaderProgram = GLES32.glCreateProgram();
        GLES32.glAttachShader(_shaderProgram, vertexShader);
        GLES32.glAttachShader(_shaderProgram, fragmentShader);
        GLES32.glLinkProgram(_shaderProgram);
        GLES32.glDetachShader(_shaderProgram, vertexShader);
        GLES32.glDetachShader(_shaderProgram, fragmentShader);
        GLES32.glDeleteShader(vertexShader);
        GLES32.glDeleteShader(fragmentShader);
    }

    public void use() {
        GLES32.glUseProgram(_shaderProgram);
    }

    public void delete() {
        GLES32.glDeleteShader(_shaderProgram);
    }

    public int getId() {
        return _shaderProgram;
    }

    // TODO: cache location
    public void setVec4Uniform(String name, float x, float y, float z, float w) {
        this.use();
        var location = GLES32.glGetUniformLocation(_shaderProgram, name);
        GLES32.glUniform4f(location, x, y, z, w);
    }

    public void setMat4Uniform(String name, float[] matrix) {
        this.use();
        var location = GLES32.glGetUniformLocation(_shaderProgram, name);
        GLES32.glUniformMatrix4fv(location, 1, false, matrix, 0);
    }

    public void setIntUniform(String name, int value) {
        this.use();
        var location = GLES32.glGetUniformLocation(_shaderProgram, name);
        GLES32.glUniform1i(location, value);
    }

    private static int loadShader(int type, String shaderCode){
        var shader = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader, shaderCode);
        GLES32.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("GL", "Could not compile shader " + type + ": " + GLES32.glGetShaderInfoLog(shader));
            GLES32.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }
}
