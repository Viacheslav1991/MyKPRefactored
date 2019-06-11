package com.bignerdranch.android.mykprefactored.objects;

import com.bignerdranch.android.mykprefactored.data.VertexArray;
import com.bignerdranch.android.mykprefactored.programs.ColorShaderProgram;
import com.bignerdranch.android.mykprefactored.util.Geometry;
import com.bignerdranch.android.mykprefactored.util.Vector;

import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glLineWidth;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Axes {
    private static final int POSITION_COMPONENT_COUNT = 3;
    //    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = 0;
    private ColorShaderProgram mColorShaderProgram;

    private final VertexArray vertexArray;

    public Axes(int length) {
        float[] vertexData = createAxes(length);
        vertexArray = new VertexArray(vertexData);
    }

    private float[] createAxes(int length) {
        float vertices[] = {
                // ось X
                -length, 0, 0,
                length, 0, 0,

                // ось Y
                0, -length, 0,
                0, length, 0,

                // ось Z
                0, 0, -length,
                0, 0, length,};
        return vertices;
    }

    public void bindData(ColorShaderProgram colorShaderProgram) {
        mColorShaderProgram = colorShaderProgram;
        vertexArray.setVertexAttribPointer(
                0,
                colorShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

       /* vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);*/
    }

    public void draw(float[] matrix) {
        glLineWidth(10);
        mColorShaderProgram.setUniforms(matrix,1,1,0);
        glDrawArrays(GL_LINES, 0, 2);
        mColorShaderProgram.setUniforms(matrix,0,1,1);
        glDrawArrays(GL_LINES, 2, 2);
        mColorShaderProgram.setUniforms(matrix,1,0,1);
        glDrawArrays(GL_LINES, 4, 2);
    }


}
