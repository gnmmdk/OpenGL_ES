package com.kangjj.opengl.es;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.kangjj.opengl.es.filters.CameraFilter;
import com.kangjj.opengl.es.filters.ScreenFilter;
import com.kangjj.opengl.es.record.MyMediaRecorder;
import com.kangjj.opengl.es.utils.CameraHelper;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

class MyGLRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private final MyGLSurfaceView mGLSurfaceView;
    private final int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private CameraHelper mCameraHelper;
    private SurfaceTexture mSurfaceTexture;
    private int[] mTextureID;
    private CameraFilter mCameraFilter;
    private ScreenFilter mScreenFilter;
    private MyMediaRecorder mMediaRecorder;


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
        mScreenFilter = new ScreenFilter(mGLSurfaceView.getContext());
        mCameraFilter = new CameraFilter(mGLSurfaceView.getContext());
        EGLContext eglContext = EGL14.eglGetCurrentContext();           //渲染线程的EGLContext
        mMediaRecorder = new MyMediaRecorder(480, 800, "sdcard/kangjjTest.mp4", eglContext, mGLSurfaceView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraHelper.startPreview(mSurfaceTexture);
        mCameraFilter.onReady(width,height);
        mScreenFilter.onReady(width,height);
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
        mCameraFilter.setMatrix(mtx);
        //mTextureID[0]: 摄像头的, 先渲染到FBO
        int textureId = mCameraFilter.onDrawFrame(mTextureID[0]);
        //滤镜特效
        //textureId = xxxFilter.onDrawFrame(textureId);
        //textureId = xxxFilter.onDrawFrame(textureId);
        //......
        mScreenFilter.onDrawFrame(textureId);

        //录制视频（将图像进行编码）
        mMediaRecorder.encodeFrame(textureId,mSurfaceTexture.getTimestamp());
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLSurfaceView.requestRender();
    }

    public void onSurfaceDestroyed() {
        mCameraHelper.stopPreview();
    }

    public void startRecording(float speed) {
        Log.e("MyGLRender", "startRecording");
        try {
            mMediaRecorder.start(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        Log.e("MyGLRender", "stopRecording");
        mMediaRecorder.stop();
    }
}
