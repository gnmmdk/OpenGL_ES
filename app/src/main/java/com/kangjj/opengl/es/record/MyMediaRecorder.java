package com.kangjj.opengl.es.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MyMediaRecorder {

    private final int mWidth;
    private final int mHeight;
    private final String mOutputPath;
    private final EGLContext mEglContext;
    private final Context mContext;
    private float mSpeed;
    private MediaCodec mMediaCodec;
    private Surface mInputSurface;
    private MediaMuxer mMediaMuxer;
    private Handler mHandler;
    private boolean isStart;
    private MyEGL mEGL;
    private int index;
    private long lastTimestampUs;

    /**
     * 在onSurfaceCreated时进行实例化
     * @param width
     * @param height
     * @param outputPath
     * @param eglContext
     * @param context
     */
    public MyMediaRecorder(int width, int height, String outputPath, EGLContext eglContext, Context context) {
        mWidth = width;
        mHeight = height;
        mOutputPath = outputPath;
        mEglContext = eglContext;
        mContext = context;
    }

    /**
     * 开始录制
     * @param speed
     * @throws IOException
     */
    public void start(float speed) throws IOException {
        mSpeed = speed;
        /**
         * 1 创建MediaCode编码器
         */
        //创建那种视频编码器（H.264)
        mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        /**
         * 2 配置编码器
         */
        //视频格式
        MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,mWidth,mHeight);
        //比特率（码率）1500kpbs
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,1500_000);
        //帧率 30fps
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30);
        //颜色格式（从surface中获取）
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //关键帧间隔
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,20);
        //配置编码器
        mMediaCodec.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        /**
         * 3 创建输入Surface（虚拟屏）
         */
        mInputSurface = mMediaCodec.createInputSurface();
        /**
         * 4 创建封装器（复用器）
         */
        mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        /**
         * 5 配置EGL环境
         */
        HandlerThread handlerThread = new HandlerThread("MyMediaRecorder");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mHandler = new Handler(looper);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEGL = new MyEGL(mEglContext,mInputSurface,mContext,mWidth,mHeight);
                mMediaCodec.start();
                isStart = true;
            }
        });
    }

    public void stop() {
        isStart = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getEncodeData(true);
                if(mMediaCodec !=null){
                    mMediaCodec.stop();
                    mMediaCodec.release();
                    mMediaCodec = null;
                }
                if(mMediaMuxer !=null){
                    try {
                        mMediaMuxer.stop();
                        mMediaMuxer.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mMediaMuxer = null;
                }
                if(mInputSurface!=null){
                    mInputSurface.release();
                    mInputSurface = null;
                }
                mEGL.release();
                mEGL = null;
                mHandler.getLooper().quitSafely();
                mHandler = null;
            }
        });
    }

    /**
     * 编码纹理图像
     * @param textureId
     * @param timestamp
     */
    public void encodeFrame(final int textureId, final long timestamp) {
        if(!isStart){
            return;
        }
        if(mHandler!=null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //把纹理图像画到虚拟屏上
                    if(mEGL != null){
                        mEGL.draw(textureId,timestamp);
                    }
                    //然后从编码器中输出缓冲区去获取编码后的数据
                    getEncodeData(false);
                }
            });
        }
    }

    /**
     * 获取编码后的数据
     * @param endOfStream 标记是否结束录制
     */
    private void getEncodeData(boolean endOfStream) {
        if(endOfStream){
            mMediaCodec.signalEndOfInputStream();//不录，发送一个通知给mMediaCodec
        }
        //输出缓冲区
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            int status = mMediaCodec.dequeueOutputBuffer(bufferInfo,10_000);//10ms
            if(status == MediaCodec.INFO_TRY_AGAIN_LATER){
                //如果是 endOfStream==true：
                if(!endOfStream){
                    //如果是endOfStream == false：结束录制，跳出循环
                    break;
                }
            }else if(status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                MediaFormat outputFormat = mMediaCodec.getOutputFormat();
                index  = mMediaMuxer.addTrack(outputFormat);
                mMediaMuxer.start();//开始封装
            }else if(status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){

            }else{
                //成功取到一个有效的数据
                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(status);
                if(outputBuffer == null){
                    throw new RuntimeException("getOutputBuffer fail: " + status);
                }
                //如果取到outputBuffer是配置信息
                if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 ){
                    bufferInfo.size=0;
                }
                if(bufferInfo.size!=0){
                    bufferInfo.presentationTimeUs = (long)(bufferInfo.presentationTimeUs / mSpeed);
                    //presentationTimeUs xxxx < lastTimestampUs  xxx
                    //时间戳比上一次的要小，这判断一下
                    if (bufferInfo.presentationTimeUs <= lastTimestampUs) {
                        bufferInfo.presentationTimeUs = (long) (lastTimestampUs + 1_000_000 / 30 / mSpeed);
                    }
                    lastTimestampUs = bufferInfo.presentationTimeUs;
                    //偏移位置
                    outputBuffer.position(bufferInfo.offset);
                    //可读写的总长度
                    outputBuffer.limit(bufferInfo.offset+bufferInfo.size);
                    //写数据（输出）
                    try {
                        mMediaMuxer.writeSampleData(index,outputBuffer,bufferInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //一定要，使用完输出缓冲区，就可以回收了，让mMediaCodec能继续使用
                mMediaCodec.releaseOutputBuffer(status,false);
                //结束
                if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0){
                    break;
                }
            }

        }//end while

    }
}
