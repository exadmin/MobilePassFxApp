package com.github.exadmin.mpcr.fxui;

import javafx.beans.property.IntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FxSetupSceneBuilder {
    private final FxSceneModel fxSceneModel;

    public FxSetupSceneBuilder(FxSceneModel fxSceneModel) {
        this.fxSceneModel = fxSceneModel;
    }

    public void showSetupStage(Stage primaryStage) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        HBox param1 = buildControl("Bilateral Filter: Diameter", 1, 100, fxSceneModel.bilateralFilterDiameterProperty);
        HBox param2 = buildControl("Bilateral Filter: Sigma Color", 0, 1000, fxSceneModel.bilateralFilterSigmaColor);
        HBox param3 = buildControl("Bilateral Filter: Sigma Space", 0, 1000, fxSceneModel.bilateralFilterSigmaColor);
        HBox param4 = buildControl("Gaussian Blur: Kernel Width", 0, 10, fxSceneModel.gaussKernelWidth);
        HBox param5 = buildControl("Gaussian Blur: Kernel Height", 0, 10, fxSceneModel.gaussKernelHeight);
        HBox param6 = buildControl("Gaussian Blur: SigmaX", 0, 100, fxSceneModel.gaussSigmaX);
        HBox param7 = buildControl("Canny Edges: Threshold 1", 0, 1000, fxSceneModel.cannyThreshold1);
        HBox param8 = buildControl("Canny Edges: Threshold 2", 0, 1000, fxSceneModel.cannyThreshold2);
        HBox param9 = buildControl("Canny Edges: Aperture size", 1, 3, fxSceneModel.cannyApertureSize);

        // HBox test2 = buildControl("Test 2");
        vbox.getChildren().addAll(param1, param2, param3, param4, param5, param6, param7, param8, param9);

        Scene scene = new Scene(vbox, 800, 600);

        final Stage setupStage = new Stage();
        setupStage.initModality(Modality.NONE);
        setupStage.initOwner(primaryStage);
        setupStage.setScene(scene);
        setupStage.show();
    }

    private HBox buildControl(String name, int minValue, int maxValue, IntegerProperty property) {
        HBox hbox = new HBox();
        hbox.setMaxWidth(Integer.MAX_VALUE);

        Label lbName = new Label(name + " : ");
        {
            lbName.setMinWidth(120);
            lbName.setPrefWidth(120);
        }

        Label lbValue = new Label();
        {
            lbValue.setMinWidth(80);
            lbValue.setPrefWidth(80);
            lbValue.textProperty().bind(property.asString());
        }

        Slider slider = new Slider(minValue, maxValue, property.getValue());
        slider.valueProperty().bindBidirectional(property);
        slider.setMaxWidth(Integer.MAX_VALUE);
        slider.setMinWidth(500);

        hbox.getChildren().addAll(lbName, lbValue, slider);
        return hbox;
    }
}
