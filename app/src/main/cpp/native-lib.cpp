#include <jni.h>
#include <string>
#include <opencv2/imgproc/types_c.h>
#include "FaceTrack.h"
#include <android/log.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_com_kangjj_opengl_es_face_FaceTrack_native_1create(JNIEnv *env, jobject thiz, jstring model_,jstring setta_) {
    const char *model = env->GetStringUTFChars(model_,0);
    const char *setta = env->GetStringUTFChars(setta_,0);
    FaceTrack * faceTrack = new FaceTrack(model,setta);
    env->ReleaseStringUTFChars(model_,model);
    env->ReleaseStringUTFChars(setta_,setta);
    return reinterpret_cast<jlong>(faceTrack);
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_kangjj_opengl_es_face_FaceTrack_native_1detector(JNIEnv *env, jobject thiz, jlong self,
                                                          jbyteArray data_, jint camera_id,
                                                          jint width, jint height) {

    if(self == 0){
        return NULL;
    }
    jbyte *data = env->GetByteArrayElements(data_,NULL);
    FaceTrack *me = (FaceTrack *)self;
    Mat src(height + height/2 , width ,CV_8UC1,data);
//    imwrite("/sdcard/src1.jpg",src);
    cvtColor(src,src,CV_YUV2RGBA_NV21);
    if(camera_id ==1 ){
        //前置摄像头，逆时针旋转90度
        rotate(src,src,ROTATE_90_COUNTERCLOCKWISE);
        //y轴
        flip(src,src,1);
    }else{
        //后置
        rotate(src,src,ROTATE_90_CLOCKWISE);
    }
    //灰度化
    cvtColor(src,src,COLOR_RGBA2GRAY);
    //直方图均衡化，增强对比效果
    equalizeHist(src,src);
    vector<Rect2f> rects;
    imwrite("/sdcard/src.jpg",src);

    //送去定位
    me->detector(src,rects);
    env->ReleaseByteArrayElements(data_,data,0);

    int imgWidth = src.cols;
    int imgHeight=  src.rows;
    src.release();
    int ret = rects.size();
    __android_log_print(ANDROID_LOG_INFO, "FaceTrack", "ret=%d",ret);
    if(ret){
        jclass clazz = env->FindClass("com/kangjj/opengl/es/face/Face");
        jmethodID construct = env->GetMethodID(clazz, "<init>", "(IIII[F)V");
        int size = ret * 2;
        //创建java的flaot数组
        jfloatArray floatArray = env->NewFloatArray(size);

        for (int i = 0,j=0; i < size; j++) {
            float f[2] = {rects[j].x , rects[j].y};
            //参数1：待赋值数组；参数2：赋值起始位置；参数3：赋值长度；参数4：数据源
            env->SetFloatArrayRegion(floatArray,i,2,f);
            i+=2;
        }
        Rect2f faceRect =rects[0];
        int faceWidth = faceRect.width;
        int faceHeigth = faceRect.height;
        jobject face = env->NewObject(clazz,construct,faceWidth,faceHeigth,imgWidth,imgHeight,floatArray);
        return face;
    }
    return NULL;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_kangjj_opengl_es_face_FaceTrack_native_1start(JNIEnv *env, jobject thiz, jlong self) {
    if(self ==0)
        return;
    FaceTrack *me = (FaceTrack*)self;
    me->startTracking();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_kangjj_opengl_es_face_FaceTrack_native_1stop(JNIEnv *env, jobject thiz, jlong self) {
    if(self ==0){
        return;
    }
    FaceTrack *me = (FaceTrack *)self;
    me->stopTracking();
    delete me;
}