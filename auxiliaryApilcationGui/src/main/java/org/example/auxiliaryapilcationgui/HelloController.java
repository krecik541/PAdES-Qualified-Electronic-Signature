package org.example.auxiliaryapilcationgui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;

/**
 * GUI controller for the RSA key generation application.
 * Allows the user to input a password (PIN) for encrypting the private key
 * and select a directory where the keys will be saved.
 */
public class HelloController {
    /**
     * Default path to the directory where the keys will be saved.
     * By default, this is the user's desktop.
     */
    private String pathToSave = Paths.get(System.getProperty("user.home"), "Desktop").toString();


    /**
     * Stage for the directory chooser dialog.
     */
    static Stage stage;

    @FXML
    private Label welcomeText;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button generateButton;

    @FXML
    private Button selectButton;

    /**
     * Handles the "Generate" button click event.
     * Creates an instance of `KeyGenerator` and generates RSA keys.
     *
     * @throws Exception If an error occurs during key generation.
     */
    @FXML
    protected void onGenerateButtonClick() throws Exception {
        KeyGenerator keyGenerator = new KeyGenerator(passwordField.getText(), pathToSave);

        keyGenerator.init();

        welcomeText.setText("RSA key has been generated!");
    }

    /**
     * Handles text changes in the password field.
     * Enables or disables the "Generate" button based on whether the password field is empty.
     */
    @FXML
    protected void onTextChanged() {
        generateButton.setDisable(passwordField.getText().isEmpty());
    }

    /**
     * Handles the "Select Directory" button click event.
     * Opens a directory chooser dialog to allow the user to select a directory for saving keys.
     */
    @FXML
    protected void onSelectButtonClick() {
        String desktop = Paths.get(System.getProperty("user.home"), "Desktop").toString();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(desktop));
        File selectedDirectory = directoryChooser.showDialog(stage);

        pathToSave = (selectedDirectory != null) ?
                selectedDirectory.getAbsolutePath() : Paths.get(System.getProperty("user.home"), "Desktop").toString();
    }
}