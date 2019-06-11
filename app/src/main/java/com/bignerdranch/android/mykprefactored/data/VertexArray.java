package com.bignerdranch.android.mykprefactored.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

public class VertexArray { // предназначен для расположения данных в native memory
    private final FloatBuffer floatBuffer;
    public VertexArray(float[] vertexData) {
        floatBuffer = ByteBuffer
                .allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }
    public void setVertexAttribPointer(int dataOffset, int attributeLocation,
                                       int componentCount, int stride) {
        floatBuffer.position(dataOffset);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT,
                false, stride, floatBuffer);//Методом glVertexAttribPointer мы сообщаем системе,
                                                    // что шейдеру для своего атрибута a_Position необходимо читать данные из массива floatBuffer.
                                                    // А параметры этого метода позволяют подробно задать правила чтения.
        glEnableVertexAttribArray(attributeLocation);//. И напоследок нам необходимо включить атрибут aPositionLocation методом glEnableVertexAttribArray.
        floatBuffer.position(0);
    }

}
