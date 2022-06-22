package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import org.opencv.videoio.VideoCapture;

public class VideoCameraWarmingThread extends MyRunnable {
    public VideoCameraWarmingThread(FxSceneModel fxSceneModel, MyRunnable nextThreadToStart) {
        super(fxSceneModel, nextThreadToStart);
    }

    @Override
    protected void runSafe() throws Exception {
        VideoCapture videoCapture = new VideoCapture();
        videoCapture.open(0);
        fxSceneModel.setVideoCapture(videoCapture);
    }
}
