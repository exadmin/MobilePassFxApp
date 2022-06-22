package com.github.exadmin.mpcr.fxui.setwizard;

import com.github.exadmin.mpcr.misc.Settings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public abstract class AbstractCompositeBuilder {
    protected final IncompleteSceneBuilder incompleteSceneBuilder;

    public AbstractCompositeBuilder(IncompleteSceneBuilder incompleteSceneBuilder) {
        this.incompleteSceneBuilder = incompleteSceneBuilder;
    }

    protected  abstract AbstractCompositeBuilder getThis();

    protected abstract void buildCompositeOnHBox(HBox hBox);

    public final IncompleteSceneBuilder doneHere() {
        // build main HBox layer
        HBox hBox = new HBox();
        incompleteSceneBuilder.addHBoxToUI(hBox);
        hBox.setPrefHeight(100d);

        buildCompositeOnHBox(hBox);

        return incompleteSceneBuilder;
    }

    protected void beautifyControlWhenFocused(Label labelToHighlight, Node focusedNode) {
        labelToHighlight.setStyle(Settings.FX_STYLE_LABEL_UNFOCUSED);

        focusedNode.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                labelToHighlight.setStyle(Settings.FX_STYLE_LABEL_FOCUSED);
            } else {
                labelToHighlight.setStyle(Settings.FX_STYLE_LABEL_UNFOCUSED);
            }
        });
    }
}
