package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.async.ThreadsSequence;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import com.github.exadmin.mpcr.misc.Settings;
import com.github.exadmin.mpcr.misc.ThreadUtils;
import javafx.application.Platform;

public class CountDownThread extends MyRunnable {

    public CountDownThread(FxSceneModel fxSceneModel, MyRunnable nextThreadToStart) {
        super(fxSceneModel, nextThreadToStart);
    }

    @Override
    protected void runSafe() {
        long tsStart = System.currentTimeMillis();
        long tsEnd   = tsStart + Settings.TIME_TO_WAIT_BEFORE_START_VPN_CONNECTING_SECONDS * 1000;
        long tsCurrent = System.currentTimeMillis();

        double k = 1d / (tsEnd - tsStart);
        while (tsCurrent < tsEnd) {
            double percentage = (tsCurrent - tsStart) * k;
            long  secondsLeft = approxSecondsLeft(tsEnd, tsCurrent);

            Platform.runLater(() -> {
                fxSceneModel.getProgressBar().setProgress(percentage);
                fxSceneModel.bigCaption.setValue("Press ESC to stop VPN connection establishing in " + secondsLeft + " seconds");
            });

            if (fxSceneModel.escKeyWasPressedRecently.getValue()) {
                fxSceneModel.digitsAreProvedByUser.setValue(false);
                break;
            }

            ThreadUtils.sleep(Settings.DELAY_BETWEEN_FRAMES_MS);
            tsCurrent = System.currentTimeMillis();
        }

        // ensure we've updated progress bar (needed when ESC is pressed)
        if (fxSceneModel.escKeyWasPressedRecently.getValue()) {
            nextThreadToStart = null;

            Platform.runLater(() -> {
                fxSceneModel.freezeRecognition.setValue(false);
                fxSceneModel.pinCode.setValue("");
                fxSceneModel.bigCaption.setValue(Settings.UI_BIG_CAPTION_DEFAULT_TEXT);

                fxSceneModel.getProgressBar().setProgress(0);

                // restart digits-recognition thread
                new ThreadsSequence()
                        .startFrom(DigitsRecognitionThread.class)
                        .start(fxSceneModel);

                fxSceneModel.escKeyWasPressedRecently.setValue(false);
            });
        } else {
            fxSceneModel.digitsAreProvedByUser.setValue(true);
        }
    }

    private static long approxSecondsLeft(long tsEnd, long tsCurrent) {
        long tsDelta = tsEnd - tsCurrent;
        return Math.round(tsDelta / 1000d);
    }
}
