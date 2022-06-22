package com.github.exadmin.mpcr.examples;

import com.github.exadmin.mpcr.fxui.setwizard.SettingsSceneBuilder;
import com.github.exadmin.mpcr.misc.Settings;
import javafx.application.Application;
import javafx.stage.Stage;

public class SettingsFxApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Settings.loadFromFile();

        new SettingsSceneBuilder(primaryStage, true).openSettingsStage();
    }
}
