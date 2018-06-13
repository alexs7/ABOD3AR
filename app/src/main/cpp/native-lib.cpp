#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace cv;

extern "C" {

JNIEXPORT void JNICALL Java_com_alexbath_abod3ar_MainActivity_gaussianBlur(JNIEnv *env,
                                                                           jobject,
                                                                           jlong sourceAddress,
                                                                           jlong targetAddress) {

    Mat &source = *(Mat *) sourceAddress;
    Mat &target = *(Mat *) targetAddress;

    GaussianBlur(source, target, Size(7, 7), 3, 3);

}
}

