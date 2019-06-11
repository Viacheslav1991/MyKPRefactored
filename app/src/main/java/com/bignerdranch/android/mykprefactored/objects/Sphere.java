package com.bignerdranch.android.mykprefactored.objects;

import android.util.Log;

import com.bignerdranch.android.mykprefactored.data.VertexArray;
import com.bignerdranch.android.mykprefactored.programs.ColorShaderProgram;
import com.bignerdranch.android.mykprefactored.util.Geometry;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Sphere {
    private static final int POSITION_COMPONENT_COUNT = 3;
    //    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = 0;

    private final VertexArray vertexArray;
    private final int numVertices;


    public Sphere(float radius, int numPoints, Geometry.Point position) {
        this.numVertices = numPoints * numPoints * 6;
        float[] vertexData = createSphere((double) radius, numPoints, numPoints, position);
        vertexArray = new VertexArray(vertexData);
    }

    public void bindData(ColorShaderProgram colorShaderProgram) {
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

    public void draw() {
        glDrawArrays(GL_TRIANGLES, 0, numVertices);
    }

    private float[] createSphere(double radius, int _lats, int _longs, Geometry.Point pointPosition) {
        float[] position = new float[]{pointPosition.x, pointPosition.y, pointPosition.z};
        int i, j;
        int lats = _lats;
        int longs = _longs;
        float[] vertices = new float[lats * longs * 6 * 3];

        int triIndex = 0;
        double sphereSize = -0.5; // -1 for half sphere
        for (i = 0; i < lats; i++) {
            double lat0 = PI * (sphereSize + (double) (i) / lats);
            double z0 = radius * sin(lat0) + position[2];
            double zr0 = cos(lat0);

            double lat1 = PI * (sphereSize + (double) (i + 1) / lats);
            double z1 = radius * sin(lat1) + position[2];
            double zr1 = cos(lat1);

            //glBegin(GL_QUAD_STRIP);
            for (j = 0; j < longs; j++) {
                double lng = 2 * PI * (double) (j - 1) / longs;
                double x = radius * cos(lng);
                double y = radius * sin(lng);

                lng = 2 * PI * (double) (j) / longs;
                double x1 = radius * cos(lng);
                double y1 = radius * sin(lng);

//                glNormal3f(x * zr0, y * zr0, z0);
//                glVertex3f(x * zr0, y * zr0, z0);
//                glNormal3f(x * zr1, y * zr1, z1);
//                glVertex3f(x * zr1, y * zr1, z1);

                /** store after calculating positions */
                float _x1 = (float) (x * zr0) + position[0];
                float _x2 = (float) (x * zr1) + position[0];
                float _x3 = (float) (x1 * zr0) + position[0];
                float _x4 = (float) (x1 * zr1) + position[0];

                float _y1 = (float) (y * zr0) + position[1];
                float _y2 = (float) (y * zr1) + position[1];
                float _y3 = (float) (y1 * zr0) + position[1];
                float _y4 = (float) (y1 * zr1) + position[1];

                // the first triangle
                vertices[triIndex * 9 + 0] = _x1;
                vertices[triIndex * 9 + 1] = _y1;
                vertices[triIndex * 9 + 2] = (float) z0;
                vertices[triIndex * 9 + 3] = _x2;
                vertices[triIndex * 9 + 4] = _y2;
                vertices[triIndex * 9 + 5] = (float) z1;
                vertices[triIndex * 9 + 6] = _x3;
                vertices[triIndex * 9 + 7] = _y3;
                vertices[triIndex * 9 + 8] = (float) z0;

                triIndex++;
                vertices[triIndex * 9 + 0] = _x3;
                vertices[triIndex * 9 + 1] = _y3;
                vertices[triIndex * 9 + 2] = (float) z0;
                vertices[triIndex * 9 + 3] = _x2;
                vertices[triIndex * 9 + 4] = _y2;
                vertices[triIndex * 9 + 5] = (float) z1;
                vertices[triIndex * 9 + 6] = _x4;
                vertices[triIndex * 9 + 7] = _y4;
                vertices[triIndex * 9 + 8] = (float) z1;

//                vertices[triIndex*9 + 0 ] = (float)(x * zr0) -1;    vertices[triIndex*9 + 1 ] = (float)(y * zr0);   vertices[triIndex*9 + 2 ] = (float) z0;
//                vertices[triIndex*9 + 3 ] = (float)(x * zr1) -1;    vertices[triIndex*9 + 4 ] = (float)(y * zr1);   vertices[triIndex*9 + 5 ] = (float) z1;
//                vertices[triIndex*9 + 6 ] = (float)(x1 * zr0) -1;   vertices[triIndex*9 + 7 ] = (float)(y1 * zr0);  vertices[triIndex*9 + 8 ] = (float) z0;
//
//                triIndex ++;
//                vertices[triIndex*9 + 0 ] = (float)(x1 * zr0) -1;   vertices[triIndex*9 + 1 ] = (float)(y1 * zr0);    vertices[triIndex*9 + 2 ] = (float) z0;
//                vertices[triIndex*9 + 3 ] = (float)(x * zr1) -1;    vertices[triIndex*9 + 4 ] = (float)(y * zr1);     vertices[triIndex*9 + 5 ] = (float) z1;
//                vertices[triIndex*9 + 6 ] = (float)(x1 * zr1) -1;    vertices[triIndex*9 + 7 ] = (float)(y1 * zr1);   vertices[triIndex*9 + 8 ] = (float) z1;

                // in this case, the normal is the same as the vertex, plus the normalization;
//                for (int kk = -9; kk<9 ; kk++) {
//                    normals[triIndex * 9 + kk] = vertices[triIndex * 9+kk];
//                    if((triIndex * 9 + kk)%3 == 2)
//                        colors[triIndex * 9 + kk] = 1;
//                    else
//                        colors[triIndex * 9 + kk] = 0;
//                }
                triIndex++;
            }
            //glEnd();
        }
        return vertices;
    }

}
