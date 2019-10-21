package com.kangjj.opengl.es.record;

import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.kangjj.opengl.es.filters.ScreenFilter;

import static android.opengl.EGL14.*;


public class MyEGL {

    private final EGLSurface mEGLSurface;
    private EGLDisplay mEglDisplay;
    private EGLConfig mEGLConfig;
    private EGLContext mEGLContext;
    private final ScreenFilter mScreenFilter;

    public MyEGL(EGLContext eglContext, Surface surface, Context context, int width, int height) {
        //1 创建EGL环境
        createEGL(eglContext);

        int[] attrib_list = {
                EGL_NONE
        };
        //2 创建窗口，绘制线程中图像，就是往这里创建的mEGLSurface上面去画
        mEGLSurface = eglCreateWindowSurface(mEglDisplay,mEGLConfig,surface,attrib_list,0);
        //让Surface与mEglDisplay发生关系（绑定）
        if(!eglMakeCurrent(mEglDisplay,mEGLSurface,mEGLSurface,mEGLContext)){
            throw new RuntimeException("eglMakeCurrent fail: " + eglGetError());
        }
        //往虚拟屏幕上滑
        mScreenFilter = new ScreenFilter(context);

        mScreenFilter.onReady(width,height);
    }

    /**
     * 标准api调用流程
     * @param eglContext
     */
    private void createEGL(EGLContext eglContext) {
        //1、获取显示设备，默认设备：手机屏幕
        mEglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
       //2初始化设备
        int[] version = new int[2];
        if(!eglInitialize(mEglDisplay,version,0,version,1)){
            throw new RuntimeException("eglInitialize fail");
        }
        // 3 选择配置
        int[] attrib_list = {
                //指定像素格式rgba
                EGL_RED_SIZE,8,
                EGL_GREEN_SIZE,8,
                EGL_BLUE_SIZE,8,
                //指定渲染api类型
                EGL_RENDERABLE_TYPE,EGL_OPENGL_ES2_BIT,//版本
                //告诉EGL，创建surface的行为必须是视频编解码器所能兼容的
                EGLExt.EGL_RECORDABLE_ANDROID,1,
                EGL_NONE //结尾符
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        if(!eglChooseConfig(mEglDisplay,
                attrib_list,
                0,
                configs,
                0,
                configs.length,
                num_config,
                0)){
            throw new RuntimeException("eglChooseConfig fail");
        }

        //4 创建上下文
        mEGLConfig = configs[0];
        int[] ctx_attrib_list ={
                EGL_CONTEXT_CLIENT_VERSION,2,//context版本
                EGL_NONE
        };
        mEGLContext = eglCreateContext(
                mEglDisplay,//显示设备
                mEGLConfig,//上面获取到的配置
                eglContext,//共享上下文，传绘制线程（GLThread）中的EGL上下文，共享资源（发生关系）
                ctx_attrib_list,
                0
        );
        if(mEGLContext == null || mEGLContext == EGL_NO_CONTEXT){
            mEGLContext = null;
            throw new RuntimeException("eglCreateContext fail");
        }
    }

    /**
     * 单位微秒
     * @param textureId
     * @param timestamp
     */
    public void draw(int textureId, long timestamp) {
        //渲染到虚拟屏幕
        mScreenFilter.onDrawFrame(textureId);
        //刷新mEGLSurface的时间戳
        //如果设置不合理，编码的时候会采取丢帧或以低质量的编码方式进行编码
        EGLExt.eglPresentationTimeANDROID(mEglDisplay,mEGLSurface,timestamp);
        //交换缓冲数据（看资料《EGL接口解析与理解 》eglSwapBuffers接口实现说明）
        eglSwapBuffers(mEglDisplay,mEGLSurface);
    }

    public void release() {
        eglMakeCurrent(mEglDisplay,EGL_NO_SURFACE,EGL_NO_SURFACE,EGL_NO_CONTEXT);
        eglDestroySurface(mEglDisplay,mEGLSurface);
        eglDestroyContext(mEglDisplay,mEGLContext);
        eglReleaseThread();
        eglTerminate(mEglDisplay);
    }
}
