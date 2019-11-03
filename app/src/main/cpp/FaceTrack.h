//
// Created by PC on 2019/10/23.
//

#ifndef INC_3_4_1_OPENGL_ES_FACETRACK_H
#define INC_3_4_1_OPENGL_ES_FACETRACK_H

#include <opencv2/opencv.hpp>
#include <vector>
#include "FaceAlignment/include/face_alignment.h"

using namespace std;
using namespace cv;
//从 E:\opencv-4.1.1\opencv\sources\samples\android\face-detection\jni\DetectionBasedTracker_jni.cpp 拷贝
class CascadeDetectorAdapter: public DetectionBasedTracker::IDetector
{
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector):
            IDetector(),
            Detector(detector)
    {
        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects)
    {
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize, maxObjSize);
    }

    virtual ~CascadeDetectorAdapter()
    {
    }

private:
    CascadeDetectorAdapter();
    cv::Ptr<cv::CascadeClassifier> Detector;
};

class FaceTrack {
public:
    // model: opencv的模型
    // seeta：seeta的模型
    FaceTrack(const char *model,const char *seeta);

    void detector(Mat src,vector<Rect2f> &rects);

    void startTracking();

    void stopTracking();

private:
    Ptr<DetectionBasedTracker> tracker;
    Ptr<seeta::FaceAlignment> faceAlignment;
};


#endif //INC_3_4_1_OPENGL_ES_FACETRACK_H
