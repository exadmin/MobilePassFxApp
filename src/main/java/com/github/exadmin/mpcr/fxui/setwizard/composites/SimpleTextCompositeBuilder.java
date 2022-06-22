package com.github.exadmin.mpcr.fxui.setwizard.composites;

import com.github.exadmin.mpcr.fxui.setwizard.IncompleteSceneBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class SimpleTextCompositeBuilder extends AbstractGridBasedComposite {
    private StringProperty boundStringProperty;
    private String captionText;

    public SimpleTextCompositeBuilder(IncompleteSceneBuilder incompleteSceneBuilder) {
        super(incompleteSceneBuilder);
    }

    @Override
    protected SimpleTextCompositeBuilder getThis() {
        return this;
    }

    public SimpleTextCompositeBuilder setCaptionText(String captionText) {
        this.captionText = captionText;
        return getThis();
    }

    public SimpleTextCompositeBuilder bindStringProperty(StringProperty stringProperty) {
        boundStringProperty = stringProperty;
        return getThis();
    }

    @Override
    protected void buildCompositeOnGridPane2x2(GridPane gridPane) {
        Label lbCaption = new Label(captionText);
        TextField tfValue = new TextField();

        gridPane.add(lbCaption, 0, 0, 2, 1);
        gridPane.add(tfValue, 0, 1, 2, 1);

        beautifyControlWhenFocused(lbCaption, tfValue);

        if (boundStringProperty != null)
            Bindings.bindBidirectional(tfValue.textProperty(), boundStringProperty);
    }
}
