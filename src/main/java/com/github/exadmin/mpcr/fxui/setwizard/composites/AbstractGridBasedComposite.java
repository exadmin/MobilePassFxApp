package com.github.exadmin.mpcr.fxui.setwizard.composites;

import com.github.exadmin.mpcr.fxui.setwizard.AbstractCompositeBuilder;
import com.github.exadmin.mpcr.fxui.setwizard.IncompleteSceneBuilder;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public abstract class AbstractGridBasedComposite extends AbstractCompositeBuilder {
    public AbstractGridBasedComposite(IncompleteSceneBuilder incompleteSceneBuilder) {
        super(incompleteSceneBuilder);
    }

    @Override
    protected final void buildCompositeOnHBox(HBox hBox) {
        GridPane gridPane = new GridPane();
        hBox.getChildren().add(gridPane);
        {
            gridPane.setHgap(8);
            gridPane.setVgap(8);
            gridPane.setGridLinesVisible(false);
            gridPane.prefWidthProperty().bind(hBox.widthProperty());

            ColumnConstraints colCon1 = new ColumnConstraints();
            colCon1.setHgrow(Priority.ALWAYS);

            ColumnConstraints colCon2 = new ColumnConstraints();
            colCon2.setPrefWidth(100d);

            gridPane.getColumnConstraints().addAll(colCon1, colCon2);
        }

        buildCompositeOnGridPane2x2(gridPane);
    }

    protected abstract void buildCompositeOnGridPane2x2(GridPane gridPane);
}
