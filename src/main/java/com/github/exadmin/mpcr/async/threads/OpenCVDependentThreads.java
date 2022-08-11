package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.async.ThreadsSequence;
import com.github.exadmin.mpcr.fxui.FxSceneModel;

public class OpenCVDependentThreads extends MyRunnable {
    public OpenCVDependentThreads(FxSceneModel fxSceneModel, MyRunnable nextThreadToStart) {
        super(fxSceneModel, nextThreadToStart);
    }

    @Override
    protected void runSafe() {
        new ThreadsSequence()
                .startFrom(VideoCameraWarmingThread.class)
                .start(fxSceneModel);

        new ThreadsSequence()
                .startFrom(NNTrainingThread.class)
                .thenRun(DigitsRecognitionThread.class)
                .start(fxSceneModel);
    }
}
