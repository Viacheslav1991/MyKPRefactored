package com.bignerdranch.android.mykprefactored;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.bignerdranch.android.mykprefactored.objects.Axes;
import com.bignerdranch.android.mykprefactored.objects.FullCylinder;
import com.bignerdranch.android.mykprefactored.objects.FullTorus;
import com.bignerdranch.android.mykprefactored.objects.ObjectBuilder;
import com.bignerdranch.android.mykprefactored.objects.Sphere;
import com.bignerdranch.android.mykprefactored.objects.Tetrahedron;
import com.bignerdranch.android.mykprefactored.programs.ColorShaderProgram;
import com.bignerdranch.android.mykprefactored.util.Geometry;
import com.bignerdranch.android.mykprefactored.util.Vector;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_ALWAYS;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_EQUAL;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_KEEP;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_REPLACE;
import static android.opengl.GLES20.GL_STENCIL_BUFFER_BIT;
import static android.opengl.GLES20.GL_STENCIL_TEST;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glStencilFunc;
import static android.opengl.GLES20.glStencilOp;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.Matrix.setIdentityM;
import static java.lang.Math.*;

public class OpenGLRenderer implements Renderer, Serializable {

    private final static int POSITION_COUNT = 3;
    private final static int FLOATS_PER_VERTEX = 3;

    private final static String TAG = "OpenGLRenderer";

    private Context context;

    private FloatBuffer vertexData;
    private int uColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;
    private int programId;
    private final static long TIME = 10000L;
    private float tetrahedronAngleMultiplier = 2.5f;
    private float sphereAngleMultiplier = 2.5f;
    private int offset = 0;
    private float verticesInCylinder[];


    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMatrix = new float[16];

    private int mWidth;
    private float mAngleX = (float) (0.5 * PI);
    private float mAngleY = (float) (0.5 * PI);


    private float centerX;
    private float centerY;
    private float centerZ;

    private float upX;
    private float upY;
    private float upZ;

    private float eyeX = 0;
    private float eyeY = 0;
    private float eyeZ = 4;
    private float cameraToAxesRadius = (float) sqrt(pow(eyeX, 2) + pow(eyeY, 2) + pow(eyeZ, 2));

    // Objects

    private FullTorus mTorus;
    private FullCylinder mCylinder;
    private Tetrahedron mTetrahedron;
    private Sphere mSphere;
    private Axes mAxes;
    // Programs

    private ColorShaderProgram mColorShaderProgram;

    public OpenGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        glClearColor(0f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST); // включение теста глубины фигур - показывает как одна должна заслонять другую
        glEnable(GL_STENCIL_TEST); // включение буфера трафарета

        mCylinder = new FullCylinder(0.5f, 1f, 50);
        mTorus = new FullTorus(0.15f, 0.5f, 50);
        mTetrahedron = new Tetrahedron();
        mSphere = new Sphere(1f, 50, new Geometry.Point(0, 0, 0));
        mAxes = new Axes(3);

        mColorShaderProgram = new ColorShaderProgram(context);

        /*int vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vert_shader);
        int fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader);
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        glUseProgram(programId);
        createViewMatrix();
        prepareData();
        bindData();*/
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {

        // тут мы задаем порт, как обрезать и откуда и куда смотреть
        glViewport(0, 0, width, height);
        createProjectionMatrix(width, height);
        createViewMatrix();
//        mulMatrix();
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        createViewMatrix();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        mColorShaderProgram.useProgram();

        fillStencil();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glDisable(GL_STENCIL_TEST); //отключение буфера трафарета

        drawAxes();
        drawCylinder();
        drawTorus();

        glEnable(GL_STENCIL_TEST);

        drawTetrahedron();

        /*
        //draw Sphere
        setIdentityM(mModelMatrix, 0);
        mulMatrix();
        mColorShaderProgram.setUniforms(mMatrix, 0, 1, 0);
        mSphere.bindData(mColorShaderProgram);
        mSphere.draw();



        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        createViewMatrix();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);


        fillStencil();

        // Очистка
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_STENCIL_TEST); //отключение буфера трафарета

        drawAxes();
        drawCylinder();
        drawTorus();

        glEnable(GL_STENCIL_TEST);

        drawTetrahedron();
        */

    }

    private void drawAxes() {
        setIdentityM(mModelMatrix,0);
        mulMatrix();
        mAxes.bindData(mColorShaderProgram);
        mAxes.draw(mMatrix);
    }

    private void drawCylinder() {
        setIdentityM(mModelMatrix,0);
        float angle = (float) (SystemClock.uptimeMillis() % TIME) / TIME * 360;
        Matrix.rotateM(mModelMatrix, 0, angle*sphereAngleMultiplier, 1, 0, 0);
        mulMatrix();
        mColorShaderProgram.setUniforms(mMatrix, 1f, 0f, 0f);
        mCylinder.bindData(mColorShaderProgram);
        mCylinder.draw();
    }

    private void drawTorus() {
        //draw Torus
        setIdentityM(mModelMatrix,0);
        float angle = (float) (SystemClock.uptimeMillis() % TIME) / TIME * 360;
        Matrix.rotateM(mModelMatrix, 0, angle*sphereAngleMultiplier, 1, 0, 0);
        Matrix.rotateM(mModelMatrix,0,90,1,0,0);
        mulMatrix();
        mColorShaderProgram.setUniforms(mMatrix, 0, 1, 0);
        mTorus.bindData(mColorShaderProgram);
        mTorus.draw();
    }

    private void drawTetrahedron() {
        //draw Tetrahedron
        setIdentityM(mModelMatrix,0);
        float angle = (float) (SystemClock.uptimeMillis() % TIME) / TIME * 360;
        Matrix.rotateM(mModelMatrix, 0, angle * tetrahedronAngleMultiplier, 0, 1, 1);
        mulMatrix();
        glStencilFunc(GL_EQUAL, 1, 255);
        mColorShaderProgram.setUniforms(mMatrix,0,0,1);
        mTetrahedron.bindData(mColorShaderProgram);
        mTetrahedron.draw();
    }

    private void createProjectionMatrix(int width, int height) {
        mWidth = width;
        float ratio = 1;
        float left = -1;
        float right = 1;
        float bottom = -1;
        float top = 1;
        float near = 1;
        float far = 18;
        if (width > height) {
            ratio = (float) width / height;
            left *= ratio;
            right *= ratio;
        } else {
            ratio = (float) height / width;
            bottom *= ratio;
            top *= ratio;
        }

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    private void createViewMatrix() {
        // точка положения камеры
//        eyeX = 3;
//        eyeY = 2;
//        eyeZ = 6;

        // точка направления камеры
        centerX = 0;
        centerY = 0;
        centerZ = 0;

        // up-вектор
        upX = 0;
        upY = 1;
        upZ = 0;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    private void mulMatrix() {

        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
//        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);
    }

    private void fillStencil() {
        // куб в буфер
        glStencilFunc(GL_ALWAYS, 1, 0);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        mColorShaderProgram.setUniforms(mMatrix,0,1,0);
        mTetrahedron.bindData(mColorShaderProgram);
        mTetrahedron.draw();

        // Сфера в буфер
        glStencilFunc(GL_ALWAYS, 2, 0);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        mColorShaderProgram.setUniforms(mMatrix, 0, 1, 1);
        mSphere.bindData(mColorShaderProgram);
        mSphere.draw();
    }

    public void setEye(float x, float y) {
        mAngleX -= (((2 * PI) / mWidth) * x / 20) % (2 * PI);
        mAngleY += (((2 * PI) / mWidth) * y / 20) % (2 * PI);
        setEye();
    }

    private void setEye() {
        float cameraX = (float) (cameraToAxesRadius * cos(mAngleX) * sin(mAngleY));
        float cameraY = (float) (cameraToAxesRadius * cos(mAngleY));
        float cameraZ = (float) (cameraToAxesRadius * sin(mAngleX) * sin(mAngleY));

        eyeX = cameraX;
        eyeY = cameraY;
        eyeZ = cameraZ;

        Log.i(TAG, "Angle = " + mAngleX);
        Log.i(TAG, "Radius = " + cameraToAxesRadius);
        Log.i(TAG, "camera X = " + cameraX);
        Log.i(TAG, "camera Z = " + cameraZ);
    }

    public void setTetrahedronAngleMultiplier(float multiplier) {
        tetrahedronAngleMultiplier = 5;
        tetrahedronAngleMultiplier *= multiplier;
    }

    public void setSphereAngleMultiplier(float multiplier) {
        sphereAngleMultiplier = 5;
        sphereAngleMultiplier *= multiplier;
    }

    public void zoomPlus() {
        cameraToAxesRadius--;
        setEye();
    }

    public void zoomMinus() {
        cameraToAxesRadius++;
        setEye();
    }

}


