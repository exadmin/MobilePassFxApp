package com.github.exadmin.mpcr.fxui.setwizard;

import com.github.exadmin.mpcr.fxui.setwizard.composites.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class IncompleteSceneBuilder {
    protected final List<HBox> hBoxes = new ArrayList<>();

    protected IncompleteSceneBuilder getThis() {
        return this;
    }

    public BrowseFileCompositeBuilder addBrowseFileControl() {
        return new BrowseFileCompositeBuilder(getThis());
    }

    public SimpleTextCompositeBuilder addSimpleTextControl() {
        return new SimpleTextCompositeBuilder(getThis());
    }

    public PasswordCompositeBuilder addPasswordControl() {
        return new PasswordCompositeBuilder(getThis());
    }

    public CheckboxCompositeBuilder addCheckBoxControl() {
        return new CheckboxCompositeBuilder(getThis());
    }

    public CloseButtonsCompositeBuilder addSaveCloseButtonsComposite() {
        return new CloseButtonsCompositeBuilder(getThis());
    }

    void addHBoxToUI(HBox hBox) {
        hBoxes.add(hBox);
    }

    public Scene buildScene() {
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10, 8, 10, 8));
        vBox.setSpacing(8);
        vBox.setStyle("-fx-background-color: #336699;");

        vBox.getChildren().addAll(hBoxes);

        return new Scene(vBox, 800,600);
    }
}
