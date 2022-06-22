package com.github.exadmin.mpcr.fxui.setwizard.composites;

import com.github.exadmin.mpcr.fxui.setwizard.IncompleteSceneBuilder;
import com.github.exadmin.mpcr.misc.Settings;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class CheckboxCompositeBuilder extends AbstractGridBasedComposite {
    private String captionText;
    private String checkBoxText;
    private BooleanProperty boundProperty;

    public CheckboxCompositeBuilder(IncompleteSceneBuilder incompleteSceneBuilder) {
        super(incompleteSceneBuilder);
    }

    @Override
    protected CheckboxCompositeBuilder getThis() {
        return this;
    }

    public CheckboxCompositeBuilder setCaptionText(String captionText) {
        this.captionText = captionText;
        return getThis();
    }

    public CheckboxCompositeBuilder setCheckBoxText(String checkBoxText) {
        this.checkBoxText = checkBoxText;
        return getThis();
    }

    public CheckboxCompositeBuilder bindBooleanProperty(BooleanProperty booleanProperty) {
        boundProperty = booleanProperty;
        return getThis();
    }

    @Override
    protected void buildCompositeOnGridPane2x2(GridPane gridPane) {
        Label lbCaption = new Label(captionText);

        CheckBox checkBox = new CheckBox(checkBoxText);
        if (boundProperty != null)
            checkBox.selectedProperty().bindBidirectional(boundProperty);

        gridPane.add(lbCaption, 0, 0, 2, 1);
        gridPane.add(checkBox, 0, 1, 1, 1);

        beautifyControlWhenFocused(lbCaption, checkBox);
        checkBox.setStyle(Settings.FX_STYLE_LABEL_UNFOCUSED);
    }
}
