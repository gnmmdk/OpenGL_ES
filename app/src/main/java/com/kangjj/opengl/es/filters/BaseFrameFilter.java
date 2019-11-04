package com.kangjj.opengl.es.filters;

import android.content.Context;

import com.kangjj.opengl.es.utils.TextureHelper;

import static android.opengl.GLES20.*;

public class BaseFrameFilter extends BaseFilter {
    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;

    @Override
    protected void changeTextureData() {
        float[] TEXTURE={
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
        };
        textureData.clear();
        textureData.put(TEXTURE);
    }

    public BaseFrameFilter(Context context, int vertexSourceId, int fragmentSourceId) {
        super(context, vertexSourceId, fragmentSourceId);
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
    public void release(){
        super.release();
        releaseFrameBuffers();
    }

    private void releaseFrameBuffers() {
        if(mFrameBufferTextures!=null){
            glDeleteTextures(1,mFrameBufferTextures,0);
            mFrameBufferTextures = null;
        }
        if(mFrameBuffers!=null){
            glDeleteBuffers(1,mFrameBuffers,0);
            mFrameBuffers = null;
        }
    }
}
