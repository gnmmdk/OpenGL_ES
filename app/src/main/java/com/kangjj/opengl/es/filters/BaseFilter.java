package com.kangjj.opengl.es.filters;

import android.content.Context;

import com.kangjj.opengl.es.utils.BufferHelper;
import com.kangjj.opengl.es.utils.ShaderHelper;
import com.kangjj.opengl.es.utils.TextResourceReader;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

public class BaseFilter {
    private final int mVertexSourceId;
    private final int mFragmentSourceId;
    protected final FloatBuffer vertexData;
    protected final FloatBuffer textureData;
    protected int mProgramId;
    protected int vPosition;
    protected int vCoord;
    protected int vMatrix;
    protected int vTexture;

    public BaseFilter(Context context, int vertexSourceId, int fragmentSourceId) {
        mVertexSourceId = vertexSourceId;
        mFragmentSourceId = fragmentSourceId;
        float[] VERTEX = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f,
        };
        vertexData = BufferHelper.getFloatBuffer(VERTEX);
        float[] TEXTURE = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        textureData = BufferHelper.getFloatBuffer(TEXTURE);
        init(context);
    }

    private void init(Context context) {
        //顶点着色器代码
        String vertexSource = TextResourceReader.readTextFileFromResource(context,mVertexSourceId);
        //片元着色器代码
        String fragmentSource = TextResourceReader.readTextFileFromResource(context,mFragmentSourceId);
        //编译获得着色器id
        int vertexShaderId = ShaderHelper.compileVertexShader(vertexSource);
        int fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentSource);
        //获取程序id
        mProgramId = ShaderHelper.linkProgram(vertexShaderId,fragmentShaderId);
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
        //通过程序id获取属性索引
        //定点
        vPosition = glGetAttribLocation(mProgramId,"vPosition");
        vCoord = glGetAttribLocation(mProgramId,"vCoord");
        vMatrix = glGetAttribLocation(mProgramId,"vMatrix");
        //片元
        vTexture = glGetUniformLocation(mProgramId,"vTexture");
    }
}
