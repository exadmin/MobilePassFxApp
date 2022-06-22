package com.github.exadmin.mpcr.fxui.setwizard.composites;

import com.github.exadmin.mpcr.fxui.setwizard.IncompleteSceneBuilder;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BrowseFileCompositeBuilder extends AbstractGridBasedComposite
{
    private String captionText;
    private String defaultDirectoryForFileChooser = "C:\\";

    private StringProperty boundStringProperty;
    private final Map<String, String> fileChooserExtensionFilterMap = new HashMap<>();

    public BrowseFileCompositeBuilder(IncompleteSceneBuilder incompleteSceneBuilder) {
        super(incompleteSceneBuilder);
    }

    @Override
    protected BrowseFileCompositeBuilder getThis() {
        return this;
    }

    public BrowseFileCompositeBuilder setDefaultDirectoryForFileChooser(String defaultDirectoryForFileChooser) {
        this.defaultDirectoryForFileChooser = defaultDirectoryForFileChooser;
        return getThis();
    }

    public BrowseFileCompositeBuilder setCaptionText(String captionText) {
        this.captionText = captionText;
        return getThis();
    }

    public BrowseFileCompositeBuilder bindStringProperty(StringProperty stringProperty) {
        boundStringProperty = stringProperty;
        return getThis();
    }

    public BrowseFileCompositeBuilder addFileChoosingExtensionFilter(String filterVisibleName, String fileMask) {
        fileChooserExtensionFilterMap.put(filterVisibleName, fileMask);
        return getThis();
    }

    @Override
    protected void buildCompositeOnGridPane2x2(GridPane gridPane) {
        Label lbCaption = new Label(captionText);
        TextField tfValue = new TextField();
        Button btnBrowse = new Button("Browse");

        gridPane.add(lbCaption, 0, 0, 2, 1);
        gridPane.add(tfValue, 0, 1, 1, 1);
        gridPane.add(btnBrowse, 1, 1, 1, 1);



        // Setup browse button
        btnBrowse.setPrefWidth(100d);
        if (boundStringProperty != null)
            tfValue.textProperty().bindBidirectional(boundStringProperty);

        btnBrowse.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(defaultDirectoryForFileChooser));
            fileChooser.setTitle(captionText);

            for (Map.Entry<String, String> me : fileChooserExtensionFilterMap.entrySet()) {
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(me.getKey(), me.getValue());
                fileChooser.getExtensionFilters().add(extFilter);
            }

            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                tfValue.setText(file.getAbsolutePath().trim());
            }
        });

        // on text-field focus
        beautifyControlWhenFocused(lbCaption, tfValue);
    }
}
