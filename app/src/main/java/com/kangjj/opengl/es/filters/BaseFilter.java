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
    protected int mHeight;
    protected int mWidth;

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

        //后摄：顺时针旋转90度
//            float[] TEXTURE = {
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            0.0f, 1.0f,
//            0.0f, 0.0f
//        };
        //前摄：逆时针旋转90度后镜像
//        float[] TEXTURE = {
//                0.0f, 1.0f,
//                0.0f, 0.0f,
//                1.0f, 1.0f,
//                1.0f, 0.0f
//        };
//        float[] TEXTURE = {
//                0.0f, 0.0f,
//                0.0f, 1.0f,
//                1.0f, 0.0f,
//                1.0f, 1.0f,
//        };
        float[] TEXTURE = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        textureData = BufferHelper.getFloatBuffer(TEXTURE);
        init(context);
    }

    protected void init(Context context) {
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
        vMatrix = glGetUniformLocation(mProgramId,"vMatrix");
        //片元
        vTexture = glGetUniformLocation(mProgramId,"vTexture");
    }

    public void release(){
        glDeleteProgram(mProgramId);
    }

    public void onReady(int width,int height){
        mWidth = width;
        mHeight = height;
    }

    public int onDrawFrame(int textureID){
        //1. 设置视窗
        glViewport(0,0,mWidth,mHeight);
        // 2 使用着色器程序
        glUseProgram(mProgramId);
        //渲染 传值
        // ①顶点数据
        vertexData.position(0);
        glVertexAttribPointer(vPosition,2,GL_FLOAT,false,0,vertexData);
        // 传值后激活
        glEnableVertexAttribArray(vPosition);

        //② 纹理坐标
        textureData.position(0);
        glVertexAttribPointer(vCoord,2,GL_FLOAT,false,0,textureData);
        //传值后激活
        glEnableVertexAttribArray(vCoord);

        //片元，vTexture
        //激活图层
        glActiveTexture(GL_TEXTURE);
        //绑定
        glBindTexture(GL_TEXTURE_2D,textureID);
        //传递参数
        glUniform1i(vTexture,0);

        //通过opengl绘制
        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
        return textureID;

    }
}
