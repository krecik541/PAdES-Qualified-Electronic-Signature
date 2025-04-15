package org.example.auxiliaryapilcationgui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;

public class HelloController {
    // Path to save
    private String pathToSave = Paths.get(System.getProperty("user.home"), "Desktop").toString();

    static Stage stage;

    @FXML
    private Label welcomeText;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button generateButton;

    @FXML
    private Button selectButton;

    @FXML
    protected void onGenerateButtonClick() throws Exception {
        KeyGenerator keyGenerator = new KeyGenerator(passwordField.getText(), pathToSave);

        keyGenerator.init();

        welcomeText.setText("RSA key has been generated!");
    }

    @FXML
    protected void onTextChanged() {
        generateButton.setDisable(passwordField.getText().isEmpty());
    }

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