package com.github.exadmin.mpcr.fxui.setwizard.composites;

import com.github.exadmin.mpcr.fxui.setwizard.IncompleteSceneBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

public class CloseButtonsCompositeBuilder extends AbstractGridBasedComposite {
    protected EventHandler<ActionEvent> onSaveHandler;
    protected EventHandler<ActionEvent> onCloseHandler;

    public CloseButtonsCompositeBuilder(IncompleteSceneBuilder incompleteSceneBuilder) {
        super(incompleteSceneBuilder);
    }

    @Override
    protected CloseButtonsCompositeBuilder getThis() {
        return this;
    }

    public CloseButtonsCompositeBuilder setOnSaveAction(EventHandler<ActionEvent> eventHandler) {
        this.onSaveHandler = eventHandler;
        return getThis();
    }

    public CloseButtonsCompositeBuilder setOnCloseAction(EventHandler<ActionEvent> eventHandler) {
        this.onCloseHandler = eventHandler;
        return getThis();
    }

    @Override
    protected void buildCompositeOnGridPane2x2(GridPane gridPane) {
        Separator separator =  new Separator(Orientation.HORIZONTAL);
        Button btnSave = new Button("Save");
        Button btnClose = new Button("Close");

        FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);
        flowPane.getChildren().addAll(btnClose, btnSave);
        flowPane.setAlignment(Pos.CENTER_RIGHT);
        flowPane.setPadding(new Insets(4));
        flowPane.setHgap(10d);

        gridPane.add(separator, 0, 0, 2, 1);
        gridPane.add(flowPane, 0, 1, 2, 1);

        btnClose.setPrefWidth(100d);
        btnSave.setPrefWidth(100d);

        btnClose.setOnAction(onCloseHandler);
        btnSave.setOnAction(onSaveHandler);

    }
}
