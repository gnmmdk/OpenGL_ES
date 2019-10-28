package com.kangjj.opengl.es.face;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.kangjj.opengl.es.utils.CameraHelper;

public class FaceTrack {

    static{
        System.loadLibrary("native-lib");
    }
    private CameraHelper mCameraHelper;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private long self;
    private Face mFace;

    public FaceTrack(String model,CameraHelper cameraHelper){
        this.mCameraHelper = cameraHelper;
        self = native_create(model);

        mHandlerThread = new HandlerThread("FaceTrack");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                synchronized(FaceTrack.class){
                    mFace = native_detector(self,(byte[])msg.obj,mCameraHelper.getCameraId(),800,480);
                    if(mFace !=null){
                        Log.e("FaceTrack",mFace.toString());
                    }
                }
            }
        };
    }

    public void startTrack() {
        native_start(self);
    }

    public void detector(byte[] data) {
        mHandler.removeMessages(11);
        Message message = mHandler.obtainMessage(11);
        message.obj = data;
        mHandler.sendMessage(message);
    }

    public void stopTrack() {
        synchronized (this){
            mHandlerThread.quitSafely();
            mHandler.removeCallbacksAndMessages(null);
            native_stop(self);
            self = 0;
        }
    }

    private native void native_stop(long self);

    private native Face native_detector(long self, byte[] data, int cameraId, int width, int height);

    private native long native_create(String model);


    private native void native_start(long self);

}
