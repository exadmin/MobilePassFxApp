package com.github.exadmin.mpcr;

import com.github.exadmin.mpcr.async.ThreadsSequence;
import com.github.exadmin.mpcr.async.threads.GifResourceLoaderThread;
import com.github.exadmin.mpcr.async.threads.OpenCVDependentThreads;
import com.github.exadmin.mpcr.async.threads.OpenCVLoaderThread;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import com.github.exadmin.mpcr.fxui.ModernSceneBuilder;
import com.github.exadmin.mpcr.fxui.setwizard.SettingsSceneBuilder;
import com.github.exadmin.mpcr.misc.Settings;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MobilePassCodeRecognizerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Settings.loadFromFile();
        String settingsErrorText = Settings.checkSettingsOrReturnErrorDescription();

        // Let's build & show some window to user as soon as possible
        FxSceneModel fxSceneModel = new FxSceneModel(primaryStage);

        ModernSceneBuilder fxMainSceneBuilder = new ModernSceneBuilder(fxSceneModel);
        Scene scene = fxMainSceneBuilder.buildUI();

        primaryStage.setTitle("Mobile Pass Code Recognizer, version 1.1");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.show();

        // Here we are starting some stuff in async way - to prevent user's waiting too long seeing nothing
        // Thread one: load opencv libraries, then train NN, then open Camera
        new ThreadsSequence()
                .startFrom(GifResourceLoaderThread.class)
                .start(fxSceneModel);

        if (settingsErrorText == null) {
            new ThreadsSequence()
                    .startFrom(OpenCVLoaderThread.class)
                    .thenRun(OpenCVDependentThreads.class)
                    .start(fxSceneModel);
        } else {
            new SettingsSceneBuilder(primaryStage, true).openSettingsStage();

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Not all settings are specified");
            alert.setContentText(settingsErrorText);
            alert.showAndWait();
        }
    }
}
