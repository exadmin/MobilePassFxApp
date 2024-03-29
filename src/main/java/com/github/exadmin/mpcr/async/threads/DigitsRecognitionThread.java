package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.async.ThreadsSequence;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import com.github.exadmin.mpcr.misc.Settings;
import com.github.exadmin.mpcr.misc.StrUtils;
import com.github.exadmin.mpcr.misc.ThreadUtils;
import com.github.exadmin.mpcr.recognition.DigitsFinder;
import com.github.exadmin.opencv4j.ImageUtils;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;

import java.util.List;

public class DigitsRecognitionThread extends MyRunnable {
    private boolean doVideoCapturing;
    private final DigitsFinder digitsFinder;
    private final QRCodeDetector qrCodeDetector;

    public DigitsRecognitionThread(FxSceneModel fxSceneModel, MyRunnable nextRunnable) {
        super(fxSceneModel, nextRunnable);

        this.doVideoCapturing = false;
        this.digitsFinder = new DigitsFinder(fxSceneModel);
        this.qrCodeDetector = new QRCodeDetector();
    }

    @Override
    protected void runSafe() {
        doVideoCapturing = true;

        Mat frame = new Mat();
        while (doVideoCapturing) {
            long recognitionTimeInMs = 0;

            if (fxSceneModel.getVideoCapture() != null && fxSceneModel.getVideoCapture().isOpened()) {
                // Capture one frame from video-camera
                fxSceneModel.getVideoCapture().read(frame);

                if (!frame.empty()) {

                    // Render it on the main FX form
                    Image image = ImageUtils.convertToFxImage(frame);
                    fxSceneModel.setImageAsync(image);

                    // if QR code is still not defined
                    boolean qrCodeIsDefined = StrUtils.isStringNonEmpty(fxSceneModel.passPhraseForKeyStore.getValue(), true)
                            && !fxSceneModel.passPhraseForKeyStore.getValue().equals("ERROR");

                    if (!qrCodeIsDefined) {
                        String qrCodeStr = qrCodeDetector.detectAndDecode(frame);
                        if (qrCodeStr != null && qrCodeStr.length() > 0) {

                            String possibleNTPassword = Settings.getNtPassword(qrCodeStr);
                            qrCodeIsDefined = possibleNTPassword != null;

                            Platform.runLater(() -> fxSceneModel.passPhraseForKeyStore.setValue(possibleNTPassword == null ? "ERROR" : qrCodeStr));
                        }
                    }

                    if (!fxSceneModel.freezeRecognition.getValue()) {

                        // recognize digits if can
                        long tsStart = System.currentTimeMillis();
                        final List<Integer> result = digitsFinder.findDigitsInTheImage(frame, fxSceneModel.getKNearest());
                        if (result != null && result.size() == 6) {
                            // render recognized digits
                            boolean digitsCheckPassed = true;

                            for (Integer digit : result) {
                                if (digit == null) {
                                    digitsCheckPassed = false;
                                    break;
                                }

                                if (digit < 0 || digit > 9) {
                                    digitsCheckPassed = false;
                                    break;
                                }
                            }

                            if (digitsCheckPassed) {
                                Platform.runLater(() -> {
                                    String str = "" + result.get(0) + result.get(1) + result.get(2) + result.get(3) + result.get(4) + result.get(5);
                                    fxSceneModel.pinCode.setValue(str);
                                });

                                fxSceneModel.freezeRecognition.setValue(true);

                                // We should start count-down thread to ensure that digits were recognized correctly
                                fxSceneModel.digitsAreProvedByUser.setValue(false);
                                new ThreadsSequence().startFrom(CountDownThread.class).start(fxSceneModel);
                            }
                        }
                        long tsEnd = System.currentTimeMillis();

                        recognitionTimeInMs = tsEnd - tsStart;
                        fxSceneModel.setStatusAsync("Time to process one frame = " + recognitionTimeInMs + " ms.");
                    } else {
                        fxSceneModel.setStatusAsync("Digits recognition is frozen currently");
                    }

                    // If both QR Code & 2nd Factor are recognized - then start count-down thread
                    if (qrCodeIsDefined && fxSceneModel.digitsAreProvedByUser.getValue()) {
                        new ThreadsSequence()
                                .startFrom(EstablishConnectionThread.class)
                                .start(fxSceneModel);

                        break;
                    }
                }
            }

            // Make thread sleeping desired amount of time, subtracting time which was spent for digits recognition, but check that sleep time should not less than zero
            ThreadUtils.sleep(Math.max(0, Settings.DELAY_BETWEEN_FRAMES_MS - recognitionTimeInMs));
        }
    }

    @Override
    protected void afterRunSafeFinally() {
        closeVideoCapture();
        super.afterRunSafeFinally();
    }

    private void closeVideoCapture() {
        try {
            doVideoCapturing = false;
            if (fxSceneModel.getVideoCapture() != null && fxSceneModel.getVideoCapture().isOpened()) {
                fxSceneModel.getVideoCapture().release();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
