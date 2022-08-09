package com.github.exadmin.mpcr.fxui.setwizard;

import com.github.exadmin.mpcr.misc.Settings;
import com.github.exadmin.mpcr.misc.StrUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Set;

public class SettingsSceneBuilder {
    private final Stage primaryStage;
    private final boolean terminateApplicationOnClose;

    public SettingsSceneBuilder(Stage primaryStage, boolean terminateApplicationOnClose) {
        this.primaryStage = primaryStage;
        this.terminateApplicationOnClose = terminateApplicationOnClose;
    }

    IncompleteSceneBuilder beginNewScene() {
        return new IncompleteSceneBuilder();
    }

    public void openSettingsStage() {
        final Stage myStage = new Stage();
        myStage.initModality(Modality.APPLICATION_MODAL);
        myStage.initOwner(primaryStage);

        Scene scene = buildSettingsScene(myStage);

        myStage.setScene(scene);
        myStage.setTitle("MobilePassFxApp - Settings Wizard");
        myStage.show();
    }

    private Scene buildSettingsScene(Stage myStage) {
        StringProperty vpnClientPath = new SimpleStringProperty(Settings.getVpncliPath());
        StringProperty vpnAddress = new SimpleStringProperty(Settings.getVpnHost());
        StringProperty ksMasterPass = new SimpleStringProperty("");
        StringProperty ntLogin = new SimpleStringProperty(Settings.getNtLogin());
        StringProperty ntPassword1 = new SimpleStringProperty(""); // we should never show stored NT password in UI
        StringProperty ntPassword2 = new SimpleStringProperty("");
        BooleanProperty stopAutomatically = new SimpleBooleanProperty(Settings.isAutoStopEnabled());

        EventHandler<ActionEvent> onSaveHandler = event -> {

            if (!ntPassword1.getValue().equals(ntPassword2.getValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error");
                alert.setContentText("\"NT User's Password\" values does not match. Please enter same values");
                alert.showAndWait();
                return;
            }

            if (StrUtils.isStringEmpty(ksMasterPass.getValue(), true)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error");
                alert.setContentText("It's mandatory to set keystore passphrase before saving NT-Password. Also, you have to create QR code with pass-phrase before.");
                alert.showAndWait();
                return;
            }

            Settings.setVpnCliPath(vpnClientPath.getValue());
            Settings.setVpnHost(vpnAddress.getValue());
            Settings.setNtPassword(ntPassword1.getValue(), ksMasterPass.getValue());
            Settings.setNTLogin(ntLogin.getValue());
            Settings.setAutoStopEnabled(stopAutomatically.getValue().toString());

            Exception exWhenSaving = Settings.saveSettingsToFile();
            if (exWhenSaving != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error");
                alert.setContentText("Error while saving settings, exception = " + exWhenSaving);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Done");
                alert.setContentText("Setting are saved successfully. "
                        + (terminateApplicationOnClose ? "Please restart application to apply new changes" : ""));
                alert.showAndWait();
            }
        };

        EventHandler<ActionEvent> onCloseHandler = event -> {
            if (terminateApplicationOnClose) {
                System.exit(0);
            } else {
                myStage.close();
            }
        };


        IncompleteSceneBuilder iSceneBuilder = beginNewScene();

        iSceneBuilder
                .addBrowseFileControl()
                .setCaptionText("Cisco AnyConnect Secure Mobility Client")
                .bindStringProperty(vpnClientPath)
                .addFileChoosingExtensionFilter("Other exe files (*.exe)", "*.exe")
                .addFileChoosingExtensionFilter("VPN Client (vpncli.exe)", "vpncli.exe")
                .setDefaultDirectoryForFileChooser("C:\\Program Files (x86)\\Cisco\\Cisco AnyConnect Secure Mobility Client\\")
                .doneHere();

        iSceneBuilder
                .addSimpleTextControl()
                .setCaptionText("VPN Server address to connect to")
                .bindStringProperty(vpnAddress)
                .doneHere();

        iSceneBuilder
                .addSimpleTextControl()
                .setCaptionText("NT Login to connect on behalf of")
                .bindStringProperty(ntLogin)
                .doneHere();

        iSceneBuilder
                .addPasswordControl()
                .setCaptionText("Keystore master password")
                .bindStringProperty(ksMasterPass)
                .setPromptText("Provide master password for key-store", Settings.FX_STYLE_PASSWORD_PROMPT_STYLE_NEED_INPUT)
                .doneHere();

        boolean isPasswordSet = Settings.checkNtPasswordIsSet();
        String promptText = isPasswordSet ? "Some password is already set but you can update it if needed" : "Please provide password";
        String cssStyle   = isPasswordSet ? Settings.FX_STYLE_PASSWORD_PROMPT_STYLE_VALUE_IS_SET : Settings.FX_STYLE_PASSWORD_PROMPT_STYLE_NEED_INPUT;

        iSceneBuilder
                .addPasswordControl()
                .setCaptionText("NT User's Password")
                .bindStringProperty(ntPassword1)
                .setPromptText(promptText, cssStyle)
                .doneHere();

        iSceneBuilder
                .addPasswordControl()
                .setCaptionText("NT User's Password (repeat)")
                .bindStringProperty(ntPassword2)
                .setPromptText(promptText, cssStyle)
                .doneHere();

        iSceneBuilder
                .addCheckBoxControl()
                .setCaptionText("Stop Cisco VPN agents automatically")
                .setCheckBoxText("Enable")
                .bindBooleanProperty(stopAutomatically)
                .doneHere();

        iSceneBuilder
                .addSaveCloseButtonsComposite()
                .setOnSaveAction(onSaveHandler)
                .setOnCloseAction(onCloseHandler)
                .doneHere();

        return iSceneBuilder.buildScene();
    }
}
