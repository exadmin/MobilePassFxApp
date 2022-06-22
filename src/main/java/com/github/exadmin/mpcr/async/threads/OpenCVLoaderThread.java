package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import nu.pattern.OpenCV;

/**
 * This runnable just load open-cv libraries. As it took some time (few seconds) - we make it async not to block UI
 */
public class OpenCVLoaderThread extends MyRunnable {
    public OpenCVLoaderThread(FxSceneModel fxSceneModel, MyRunnable nextRunnable) {
        super(fxSceneModel, nextRunnable);
    }

    @Override
    protected void beforeRunSafe() throws Exception {
        super.beforeRunSafe();
        fxSceneModel.setStatusAsync("Loading openCV libraries. Please wait...");
    }

    @Override
    protected void runSafe() throws Exception {
        OpenCV.loadShared();
    }

    @Override
    protected void afterRunSafe() throws Exception {
        fxSceneModel.setStatusAsync("OpenCV libraries are loaded successfully");
        super.afterRunSafe();
    }
}
