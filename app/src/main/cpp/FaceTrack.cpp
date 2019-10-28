//
// Created by PC on 2019/10/23.
//

#include "FaceTrack.h"

FaceTrack::FaceTrack(const char *model) {
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    DetectionBasedTracker::Parameters detectorParams;
    //追踪器
    tracker = makePtr<DetectionBasedTracker>(mainDetector,trackingDetector,detectorParams);
    //TODO faceAlignment
}

void FaceTrack::detector(Mat src, vector<Rect2f> &rects) {
    vector<Rect> faces;
    //src:灰度图
    tracker->process(src);
    tracker->getObjects(faces);
    if(faces.size()){
        Rect face = faces[0];
        rects.push_back(Rect2f(face.x,face.y,face.width,face.height));
        //TODO
    }
}

void FaceTrack::startTracking() {
    tracker->run();
}

void FaceTrack::stopTracking() {
    tracker->stop();
}
