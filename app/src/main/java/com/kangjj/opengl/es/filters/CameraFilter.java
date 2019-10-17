package com.kangjj.opengl.es.filters;

import android.content.Context;
import android.opengl.GLES11Ext;

import com.kangjj.opengl.es.R;
import com.kangjj.opengl.es.utils.TextureHelper;

import static android.opengl.GLES20.*;

public class CameraFilter extends BaseFilter {
    private int[] mFrameBuffers;
    private int[] mFrameBufferTextures;
    private float[] matrix;

    public CameraFilter(Context context) {
        super(context, R.raw.camera_vertex, R.raw.camera_fragment);
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);

        //FBO (frame Buffer Object )OpenGL默认情况下，在GLSurfaceView中绘制的结果是显示到屏幕上的，
        // 但是实际情况中大部分时候都不需要渲染到屏幕中去，这个FBO就是来实现这个需求的，
        // FBO可以让不渲染到屏幕当中去，而是渲染到离屏的buffer中。

        //1 创建fbo
        mFrameBuffers = new int[1];
        //int n, fbo个数
        //int[] framebuffers, 保存fbo id的数组
        //int offset 数组中第几个来保存
        glGenFramebuffers(mFrameBuffers.length,mFrameBuffers,0);
        //2 创建属于fbo的纹理
        mFrameBufferTextures = new int[1];
        //生成并配置纹理
        TextureHelper.genTextures(mFrameBufferTextures);
        //3 让fbo与纹理发生关系
        glBindTexture(GL_TEXTURE_2D,mFrameBufferTextures[0]);
        //生成2d纹理图像
        // 目标2d纹理+登记+格式+宽+高+边界+格式+数据类型（byte）+像素数据
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,width,height,0,GL_RGBA,GL_UNSIGNED_BYTE,null);
        //让fbo与纹理绑定起来
        glBindFramebuffer(GL_FRAMEBUFFER,mFrameBuffers[0]);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,mFrameBufferTextures[0],0);
        //解绑
        glBindFramebuffer(GL_FRAMEBUFFER,0);
        glBindTexture(GL_TEXTURE_2D,0);
    }

    @Override
    public int onDrawFrame(int textureID) {
        //1. 设置视窗
        glViewport(0,0,mWidth,mHeight);
        //这里是因为要渲染到FBO缓存中，而不是直接显示到屏幕上
        glBindFramebuffer(GL_FRAMEBUFFER,mFrameBuffers[0]);

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
        //3 变换矩阵
        glUniformMatrix4fv(vMatrix,1,false,matrix,0);

        //片元，vTexture
        //激活图层
        glActiveTexture(GL_TEXTURE);
        //绑定
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureID);
        //传递参数
        glUniform1i(vTexture,0);

        //通过opengl绘制
        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
        //解绑fbo
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0);
        glBindFramebuffer(GL_FRAMEBUFFER,0);

        return mFrameBufferTextures[0];//返回fbo的纹理id
    }

    public void setMatrix(float[] mtx){
        matrix = mtx;
    }
}
