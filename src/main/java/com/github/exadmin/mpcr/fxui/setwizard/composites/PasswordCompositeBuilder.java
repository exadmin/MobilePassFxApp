package com.github.exadmin.mpcr.fxui.setwizard.composites;

import com.github.exadmin.mpcr.fxui.setwizard.IncompleteSceneBuilder;
import com.github.exadmin.mpcr.misc.StrUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;

public class PasswordCompositeBuilder extends AbstractGridBasedComposite {
    private String captionText;
    private StringProperty boundStringProperty;
    private String promptText;
    private String promptCssStyle;

    public PasswordCompositeBuilder(IncompleteSceneBuilder incompleteSceneBuilder) {
        super(incompleteSceneBuilder);
    }

    @Override
    protected PasswordCompositeBuilder getThis() {
        return this;
    }

    public PasswordCompositeBuilder setCaptionText(String captionText) {
        this.captionText = captionText;
        return getThis();
    }

    public PasswordCompositeBuilder setPromptText(String promptText, String cssStyleOrNull) {
        this.promptText = promptText;
        this.promptCssStyle = cssStyleOrNull;
        return getThis();
    }

    public PasswordCompositeBuilder bindStringProperty(StringProperty stringProperty) {
        boundStringProperty = stringProperty;
        return getThis();
    }

    @Override
    protected void buildCompositeOnGridPane2x2(GridPane gridPane) {
        Label lbCaption = new Label(captionText);
        PasswordField pfValue = new PasswordField();

        gridPane.add(lbCaption, 0, 0, 2, 1);
        gridPane.add(pfValue, 0, 1, 1, 1);

        beautifyControlWhenFocused(lbCaption, pfValue);
        if (boundStringProperty != null) {
            Bindings.bindBidirectional(pfValue.textProperty(), boundStringProperty);
        }

        if (StrUtils.isStringNonEmpty(promptText, false)) {
            pfValue.setPromptText(promptText);
        }

        if (StrUtils.isStringNonEmpty(promptCssStyle, false)) {
            pfValue.setStyle(promptCssStyle);
        }
    }
}
