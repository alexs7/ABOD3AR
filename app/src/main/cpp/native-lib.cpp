//#include <jni.h>
//#include <opencv2/core/core.hpp>
//#include <opencv2/imgproc/imgproc.hpp>
//#include <opencv2/features2d/features2d.hpp>
//#include <opencv2/calib3d.hpp>
//#include <vector>
//
//using namespace cv;
//using namespace std;
//
//extern "C" {
//
//JNIEXPORT void JNICALL Java_com_alexbath_abod3ar_MainActivity_detector(JNIEnv *env,
//                                                                           jobject,
//                                                                           jlong queryImage,
//                                                                           jlong target) {
//
//    Mat &query = *(Mat *) queryImage;
//    Mat &image = *(Mat *) target;
//
////    std::vector<KeyPoint> keyPoints1, keyPoints2;
////    Mat descriptors1, descriptors2;
////    Ptr<ORB> detector = ORB::create();
////    detector->detectAndCompute(query, Mat(), keyPoints1, descriptors1);
////    detector->detectAndCompute(image, Mat(), keyPoints2, descriptors2);
//
//    vector<KeyPoint> v;
//
//    Ptr<FeatureDetector> detector = FastFeatureDetector::create(50);
//    detector->detect(query, v);
//    for (unsigned int i = 0; i < v.size(); i++) {
//        const KeyPoint& kp = v[i];
//        circle(image, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
//    }
//
//}
//
//JNIEXPORT void JNICALL Java_com_alexbath_abod3ar_MainActivity_gaussianBlur(JNIEnv *env,
//                                                                           jobject,
//                                                                           jlong sourceAddress,
//                                                                           jlong targetAddress) {
//
//    Mat &source = *(Mat *) sourceAddress;
//    Mat &target = *(Mat *) targetAddress;
//
//    GaussianBlur(source, target, Size(7, 7), 3, 3);
//
//    }
//}
//
