package com.kangjj.opengl.es;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.kangjj.opengl.es.face.FaceTrack;
import com.kangjj.opengl.es.filters.BeautyFilter;
import com.kangjj.opengl.es.filters.BigEyeFilter;
import com.kangjj.opengl.es.filters.CameraFilter;
import com.kangjj.opengl.es.filters.ScreenFilter;
import com.kangjj.opengl.es.filters.StickFilter;
import com.kangjj.opengl.es.record.MyMediaRecorder;
import com.kangjj.opengl.es.utils.CameraHelper;
import com.kangjj.opengl.es.utils.FileUtil;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

class MyGLRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener,Camera.PreviewCallback {

    private final MyGLSurfaceView mGLSurfaceView;
    private final int mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private CameraHelper mCameraHelper;
    private SurfaceTexture mSurfaceTexture;
    private int[] mTextureID;
    private CameraFilter mCameraFilter;
    private ScreenFilter mScreenFilter;
    private BigEyeFilter mBigEyeFilter;
    private BeautyFilter mBeautyFilter;
    private MyMediaRecorder mMediaRecorder;
    private FaceTrack mFaceTrack;
    private StickFilter mStickFilter;

    private static final String SDCARD = "/sdcard";
    private static final String FRONTALFACE = "lbpcascade_frontalface.xml";
    private static final String SETTA_FA = "seeta_fa_v1.1.bin";
    private int mWidth;
    private int mHeight;

    public MyGLRenderer(MyGLSurfaceView glSurfaceView) {
        this.mGLSurfaceView = glSurfaceView;
        FileUtil.copyAssets2SDCard(mGLSurfaceView.getContext(),FRONTALFACE,
                SDCARD+"/"+FRONTALFACE);
        FileUtil.copyAssets2SDCard(mGLSurfaceView.getContext(),SETTA_FA,SDCARD+"/"+SETTA_FA);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraHelper = new CameraHelper((Activity) mGLSurfaceView.getContext(),mCameraID,720,480);
        mCameraHelper.setPreviewCallback(this);
        //准备画布
        mTextureID = new int[1];
        //通过opengl创建一个纹理的id
        glGenTextures(mTextureID.length,mTextureID,0);
        mSurfaceTexture = new SurfaceTexture(mTextureID[0]);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mScreenFilter = new ScreenFilter(mGLSurfaceView.getContext());
//        mBigEyeFilter = new BigEyeFilter(mGLSurfaceView.getContext());
        mCameraFilter = new CameraFilter(mGLSurfaceView.getContext());
        EGLContext eglContext = EGL14.eglGetCurrentContext();           //渲染线程的EGLContext
        mMediaRecorder = new MyMediaRecorder(480, 800, "sdcard/kangjjTest.mp4", eglContext, mGLSurfaceView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        //创建跟踪器
        mFaceTrack = new FaceTrack(SDCARD+"/"+FRONTALFACE,SDCARD+"/"+SETTA_FA,mCameraHelper);
        mFaceTrack.startTrack();

        mCameraHelper.startPreview(mSurfaceTexture);
        mCameraFilter.onReady(width,height);
//        mBigEyeFilter.onReady(width,height);
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
//        mBigEyeFilter.setFace(mFaceTrack.getFace());
//        textureId = mBigEyeFilter.onDrawFrame(textureId);


        if(null != mBigEyeFilter){
            mBigEyeFilter.setFace(mFaceTrack.getFace());
            textureId = mBigEyeFilter.onDrawFrame(textureId);
        }
        if(null != mStickFilter){
            mStickFilter.setFace(mFaceTrack.getFace());
            textureId = mStickFilter.onDrawFrame(textureId);
        }
        if(null != mBeautyFilter){
            textureId = mBeautyFilter.onDrawFrame(textureId);
        }
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
        mFaceTrack.stopTrack();
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

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mFaceTrack.detector(data);
    }

    public void enableBigEye(final boolean isChecked) {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if(isChecked){
                    mBigEyeFilter = new BigEyeFilter(mGLSurfaceView.getContext());
                    mBigEyeFilter.onReady(mWidth,mHeight);
                }else{
                    mBigEyeFilter.release();
                    mBigEyeFilter = null;
                }
            }
        });
    }

    public void enableStick(final boolean isChecked) {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if(isChecked){
                    mStickFilter = new StickFilter(mGLSurfaceView.getContext());
                    mStickFilter.onReady(mWidth,mHeight);
                }else{
                    mStickFilter.release();
                    mStickFilter = null;
                }
            }
        });
    }

    public void enableBeauty(final boolean isChecked) {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if(isChecked){
                    mBeautyFilter = new BeautyFilter(mGLSurfaceView.getContext());
                    mBeautyFilter.onReady(mWidth,mHeight);
                }else{
                    mBeautyFilter.release();
                    mBeautyFilter = null;
                }
            }
        });
    }
}
