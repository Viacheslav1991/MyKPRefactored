package com.bignerdranch.android.mykprefactored.objects;

import com.bignerdranch.android.mykprefactored.data.VertexArray;
import com.bignerdranch.android.mykprefactored.programs.ColorShaderProgram;
import com.bignerdranch.android.mykprefactored.programs.TextureShaderProgram;
import com.bignerdranch.android.mykprefactored.util.Vector;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static com.bignerdranch.android.mykprefactored.Constants.BYTES_PER_FLOAT;

public class TetrahedronTexture {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;


    private static final float[] VERTEX_DATA = findCoordinatesOfBottomTetrahedron();

    private final VertexArray vertexArray;

    public TetrahedronTexture() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(TextureShaderProgram textureShaderProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                textureShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureShaderProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 6);
    }

    private static float[] findCoordinatesOfBottomTetrahedron() {
        float x0 = 1;
        float z0 = 1;
        Vector vectors[] = new Vector[4];
        for (int i = 0; i <= 3; i++) {
            vectors[i] = (new Vector((float) (x0 + 2 * Math.sin(2 * Math.PI * i / 3)),
                    z0 + 2 * (float) Math.cos(2 * Math.PI * i / 3)));
        }
        float[] vertices =
                {//TetrahedronTexture:
                        // first triangle
                        vectors[0].getX() - 1, -0.65f, vectors[0].getZ() - 1,0,1,
                        0f, 2f, 0f,0.5f,1f,
                        vectors[1].getX() - 1, -0.65f, vectors[1].getZ() - 1,0,0,
                        // second triangle
                        vectors[2].getX() - 1, -0.65f, vectors[2].getZ() - 1,0,1,
                        //third triangle
                        vectors[0].getX() - 1, -0.65f, vectors[0].getZ() - 1,0.5f,1,
                        //fourth triangle
                        0f, 2f, 0f,0,0
                };
        return vertices;
    }

}
