package com.github.exadmin.mpcr.fxui;

import com.github.exadmin.mpcr.fxui.setwizard.SettingsSceneBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ModernSceneBuilder {

    private final FxSceneModel fxSceneModel;

    public ModernSceneBuilder(FxSceneModel fxSceneModel) {
        this.fxSceneModel = fxSceneModel;
    }

    public Scene buildUI()  {
        // Creating Menu Bar
        MenuBar menuBar = new MenuBar();
        Menu menuOptions = new Menu("Options");
        {
            MenuItem menuItemOpenSettings = new MenuItem("Settings");
            menuOptions.getItems().add(menuItemOpenSettings);
            menuItemOpenSettings.setOnAction(event -> new SettingsSceneBuilder(fxSceneModel.getPrimaryStage(), false).openSettingsStage());

            MenuItem menuItemOpenDebugWindow = new MenuItem("Open Recognition Debug Window");
            menuOptions.getItems().add(menuItemOpenDebugWindow);
            menuItemOpenDebugWindow.setOnAction(event -> {
                new FxDebugSceneBuilder(fxSceneModel).openDebugStage(fxSceneModel.getPrimaryStage());
                fxSceneModel.setDebugFlagEnabled(true);
            });

            MenuItem menuItemCalibrationWin = new MenuItem("Open Camera Calibration Window");
            menuOptions.getItems().add(menuItemCalibrationWin);
            menuItemCalibrationWin.setOnAction(event -> new FxSetupSceneBuilder(fxSceneModel).showSetupStage(fxSceneModel.getPrimaryStage()));

            SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
            menuOptions.getItems().add(separatorMenuItem);

            MenuItem menuItemExit = new MenuItem("Exit");
            menuOptions.getItems().add(menuItemExit);
            menuItemExit.setOnAction(event -> System.exit(0));
        }

        Menu menuHelp = new Menu("Help");
        {
            MenuItem menuItemAbout = new MenuItem("About");
            menuHelp.getItems().add(menuItemAbout);
        }

        menuBar.getMenus().addAll(menuOptions, menuHelp);



        // Building bar with welcome-label
        HBox hBoxTopBar = new HBox();
        hBoxTopBar.setAlignment(Pos.BASELINE_CENTER);
        {
            Label lbCaption = new Label("Show me your second factor from Mobile Pass+ Application");
            hBoxTopBar.getChildren().add(lbCaption);
            lbCaption.setStyle("-fx-font-size:24");
        }

        // Building image-view var
        HBox hBoxImageView = new HBox();
        hBoxImageView.setAlignment(Pos.BASELINE_CENTER);
        hBoxImageView.setFillHeight(true);
        hBoxImageView.setMaxHeight(Double.MAX_VALUE);
        {
            fxSceneModel.imgView = new ImageView();
            hBoxImageView.getChildren().add(fxSceneModel.imgView);
            fxSceneModel.imgView.fitWidthProperty().bind(hBoxImageView.widthProperty().subtract(8));
            fxSceneModel.imgView.fitHeightProperty().bind(hBoxImageView.heightProperty().subtract(8));

            fxSceneModel.imgView.setPreserveRatio(true);
        }

        // Building bar with recognized digits
        HBox hBoxDigits = new HBox();
        hBoxDigits.setAlignment(Pos.BASELINE_CENTER);
        {
            final String DEFAULT_TEXT = "X X X - X X X";
            final Label lbDigits = new Label(DEFAULT_TEXT);
            hBoxDigits.getChildren().add(lbDigits);
            lbDigits.setStyle("-fx-font-size:24");

            // bind label text to pin-code property from the model
            fxSceneModel.pinCode.addListener((observableValue, oldValue, newValue) -> {
                if (newValue == null || newValue.length() != 6) {
                    lbDigits.setText(DEFAULT_TEXT);
                    return;
                }

                String text = newValue.charAt(0) + " " + newValue.charAt(1) + " " + newValue.charAt(2)
                        + " - "
                        + newValue.charAt(3) + " " + newValue.charAt(4) + " " + newValue.charAt(5);
                lbDigits.setText(text);
            });
        }

        // Building connection progress-bar
        TitledPane tpConnection = new TitledPane();
        tpConnection.setCollapsible(false);
        tpConnection.textProperty().bind(fxSceneModel.bigCaption);
        {
            VBox vBox = new VBox();
            vBox.setPadding(new Insets(4));
            vBox.setSpacing(4);
            tpConnection.setContent(vBox);

            fxSceneModel.progressBar = new ProgressBar();
            fxSceneModel.textArea = new TextArea();
            vBox.getChildren().addAll(fxSceneModel.progressBar, fxSceneModel.textArea);

            fxSceneModel.progressBar.prefWidthProperty().bind(tpConnection.widthProperty().subtract(8));
            fxSceneModel.progressBar.setProgress(0);
        }

        // Building status-bar
        HBox hBoxStatus = new HBox();
        {
            hBoxStatus.setPadding(new Insets(4));
            fxSceneModel.lbStatus = new Label("Ready");
            hBoxStatus.getChildren().add(fxSceneModel.lbStatus);
        }

        VBox vBox = new VBox();
        vBox.getChildren().addAll(menuBar, hBoxTopBar, hBoxImageView, hBoxDigits, tpConnection, hBoxStatus);
        VBox.setVgrow(hBoxImageView, Priority.ALWAYS);

        Scene scene = new Scene(vBox, 800, 600);

        // Adding ability to press Esc - to stop connection attempt
        scene.setOnKeyPressed(keyEvent -> {
            if (KeyCode.ESCAPE.equals(keyEvent.getCode())) {
                fxSceneModel.escKeyWasPressedRecently.setValue(true);
            }
        });

        return scene;
    }
}
