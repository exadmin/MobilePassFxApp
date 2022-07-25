package com.github.exadmin.mpcr.fxui;

import com.github.exadmin.mpcr.misc.Settings;
import com.github.exadmin.opencv4j.ImageUtils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.ml.KNearest;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.atomic.AtomicReference;

public class FxSceneModel {
    private final AtomicReference<Image> imageReference = new AtomicReference<>();
    private final AtomicReference<KNearest> kNearestReference = new AtomicReference<>();

    private final Stage primaryStage;

    // Debug fields
    ImageView[][] imageViews;
    Label[][] labels;

    Label lbStatus;
    ImageView imgView;
    private boolean debugFlagEnabled = false;
    ProgressBar progressBar;
    TextArea textArea;

    VideoCapture videoCapture;

    public final StringProperty pinCode = new SimpleStringProperty();
    public final BooleanProperty freezeRecognition = new SimpleBooleanProperty(false);
    public final StringProperty bigCaption = new SimpleStringProperty(Settings.UI_BIG_CAPTION_DEFAULT_TEXT);

    // image processing variables
    public final IntegerProperty bilateralFilterDiameterProperty = new SimpleIntegerProperty(3);
    public final IntegerProperty bilateralFilterSigmaColor = new SimpleIntegerProperty(17);

    public final IntegerProperty bilateralFilterSigmaSpace = new SimpleIntegerProperty(17);
    public final IntegerProperty gaussKernelWidth = new SimpleIntegerProperty(3);
    public final IntegerProperty gaussKernelHeight = new SimpleIntegerProperty(3);
    public final IntegerProperty gaussSigmaX = new SimpleIntegerProperty(10);
    public final IntegerProperty cannyThreshold1 = new SimpleIntegerProperty(30);
    public final IntegerProperty cannyThreshold2 = new SimpleIntegerProperty(200);
    public final IntegerProperty cannyApertureSize = new SimpleIntegerProperty(2);

    public final BooleanProperty escKeyWasPressedRecently = new SimpleBooleanProperty(false);
    private final BooleanProperty disableCLICall = new SimpleBooleanProperty(false);

    public FxSceneModel(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setStatusAsync(String text) {
        if (lbStatus != null) {
            Platform.runLater(() -> lbStatus.setText(text));
        }
    }

    public void setImageAsync(Image image) {
        if (imgView != null) {
            imageReference.set(image);

            Platform.runLater(() -> {
                Image img = imageReference.get();
                if (img != null) {
                    imgView.setImage(img);
                }
            });
        }
    }

    public KNearest getKNearest() {
        return kNearestReference.get();
    }

    public void setKNearest(KNearest kNearest) {
        kNearestReference.set(kNearest);
    }


    public void setDebugImageAsync(Image image, String text, int row, int col) {
        if (imageViews != null && labels != null) {
            Platform.runLater(() -> {
                imageViews[row][col].setImage(image);
                labels[row][col].setText(text);
            });
        }
    }

    public void setDebugImageAsync(Mat matrix, String text, int row, int col) {
        if (imageViews != null && labels != null && matrix != null) {
            Image image = ImageUtils.convertToFxImage(matrix);

            Platform.runLater(() -> {
                imageViews[row][col].setImage(image);
                labels[row][col].setText(text);
            });
        }
    }

    public VideoCapture getVideoCapture() {
        return videoCapture;
    }

    public void setVideoCapture(VideoCapture videoCapture) {
        this.videoCapture = videoCapture;
    }

    public boolean isDebugFlagEnabled() {
        return debugFlagEnabled;
    }

    public void setDebugFlagEnabled(boolean debugFlagEnabled) {
        this.debugFlagEnabled = debugFlagEnabled;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setDisableCLICall(boolean value) {
        disableCLICall.setValue(value);
    }

    public boolean isCLICallDisabled() {
        return disableCLICall.getValue();
    }

    public void printToConsole(String message) {
        Platform.runLater(() -> {
            getTextArea().appendText(message + "\n");

            // attempt to scroll down
            getTextArea().setScrollTop(Double.MAX_VALUE);
        });
    }
}
