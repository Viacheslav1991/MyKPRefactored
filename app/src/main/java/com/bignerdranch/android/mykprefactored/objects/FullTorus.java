package com.bignerdranch.android.mykprefactored.objects;

import com.bignerdranch.android.mykprefactored.data.VertexArray;
import com.bignerdranch.android.mykprefactored.programs.ColorShaderProgram;
import com.bignerdranch.android.mykprefactored.util.Geometry;

import java.util.List;

public class FullTorus {
    private static final int POSITION_COMPONENT_COUNT = 3;

    public final float r, R;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public FullTorus(float r, float R, int numPoints) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createTorus(r,R,numPoints);
        this.r = r;
        this.R = R;

        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttribPointer(0,
                colorProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }
    public void draw() {
        for (ObjectBuilder.DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
