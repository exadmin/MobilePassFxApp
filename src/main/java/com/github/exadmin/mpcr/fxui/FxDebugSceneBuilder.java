package com.github.exadmin.mpcr.fxui;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FxDebugSceneBuilder {
    private final FxSceneModel fxSceneModel;


    public FxDebugSceneBuilder(FxSceneModel fxSceneModel) {
        this.fxSceneModel = fxSceneModel;
    }

    public void openDebugStage(Stage primaryStage) {
        final Stage debugStage = new Stage();
        debugStage.initModality(Modality.NONE);
        debugStage.initOwner(primaryStage);

        fxSceneModel.imageViews = new ImageView[3][3];
        fxSceneModel.labels     = new Label[3][3];

        GridPane gridPane = new GridPane();
        for (int row=0; row<3; row++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33);
            gridPane.getColumnConstraints().add(cc);

            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(33);
            gridPane.getRowConstraints().add(rc);

            for (int col=0; col<3; col++) {
                ImageView imageView = new ImageView();
                fxSceneModel.imageViews[row][col] = imageView;
                fxSceneModel.labels[row][col] = new Label("row=" + row + " : col=" + col);
                gridPane.add(fxSceneModel.imageViews[row][col], col, row);
                gridPane.add(fxSceneModel.labels[row][col], col, row);

                imageView.fitHeightProperty().bind(gridPane.heightProperty().divide(3));
                imageView.fitWidthProperty().bind(gridPane.widthProperty().divide(3));
                imageView.setPreserveRatio(true);
            }


        }

        gridPane.setGridLinesVisible(true);
        gridPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        Scene dialogScene = new Scene(gridPane, 1024, 768);
        debugStage.setScene(dialogScene);
        debugStage.show();

        dialogScene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            System.out.println("Key pressed");
            if (event.getCode() == KeyCode.F1) {
                fxSceneModel.setDebugFlagEnabled(!fxSceneModel.isDebugFlagEnabled());
            }
            event.consume();
        });
    }
}
