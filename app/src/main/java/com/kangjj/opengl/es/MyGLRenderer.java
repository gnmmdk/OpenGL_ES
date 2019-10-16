package com.kangjj.opengl.es;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import com.kangjj.opengl.es.utils.CameraHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

class MyGLRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private final MyGLSurfaceView mGLSurfaceView;
    private final int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private CameraHelper mCameraHelper;
    private SurfaceTexture mSurfaceTexture;
    private int[] mTextureID;
//    private CameraFilter mCameraFilter;
//    private ScreenFilter mScreenFilter;


    public MyGLRenderer(MyGLSurfaceView glSurfaceView) {
        this.mGLSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraHelper = new CameraHelper((Activity) mGLSurfaceView.getContext(),mCameraID,480,800);
        //准备画布
        mTextureID = new int[1];
        //通过opengl创建一个纹理的id
        glGenTextures(mTextureID.length,mTextureID,0);
        mSurfaceTexture = new SurfaceTexture(mTextureID[0]);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        //TODO
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraHelper.startPreview(mSurfaceTexture);
        //TODO
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //设置清理屏幕的颜色
        glClearColor(255,0,0,0);
        //GL_COLOR_BUFFER_BIT 颜色缓冲区
        //GL_DEPTH_WRITEMASK    深度缓冲区
        //GL_STENCIL_BUFFER_BIT 模型缓冲区
        glClear(GL_STENCIL_BUFFER_BIT);
        //输出摄像头数据
        //更新纹理
        mSurfaceTexture.updateTexImage();
        float[] mtx = new float[16];
        mSurfaceTexture.getTransformMatrix(mtx);
        //TODO
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLSurfaceView.requestRender();
    }
}
