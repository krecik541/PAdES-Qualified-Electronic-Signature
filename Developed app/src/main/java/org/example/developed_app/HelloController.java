package org.example.developed_app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static java.lang.Thread.sleep;

public class HelloController {
    private static final String DESKTOP = Paths.get(System.getProperty("user.home"), "Desktop").toString();

    static boolean signed;

    private Sign sign = new Sign();
    private Verify verify = new Verify();

    static Stage stage;

    @FXML
    private Label pendriveLabel;

    @FXML
    private Label wrongLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signSelectButton;

    @FXML
    private Button signButton;

    @FXML
    private Button verifyDocumentSelectButton;

    @FXML
    private Button verifyKeySelectButton;

    @FXML
    private Button verifyButton;

    public HelloController() {
        USBDetector usbDetector = new USBDetector();
        usbDetector.sign = sign;
        usbDetector.controller = this;
        Thread thread = new Thread(usbDetector);
        thread.start();
    }

    public void tryTo() {
        signButton.setDisable(!(!passwordField.getText().isEmpty()
                && sign.isPendriveRecognized()
                && !sign.getDocumentPath().isEmpty()));
        pendriveLabel.setDisable(!sign.isPendriveRecognized());
        pendriveLabel.setText(sign.isPendriveRecognized() ? "Pendrive is recognized" : "Pendrive is not recognized");
    }

    public void tryToVerify() {
        System.out.println(verify.getDocumentPath().isEmpty());
        System.out.println(verify.getKeyPath().isEmpty());
        System.out.println(!(verify.getDocumentPath().isEmpty() && verify.getKeyPath().isEmpty()));
        verifyButton.setDisable((verify.getDocumentPath().isEmpty() && verify.getKeyPath().isEmpty()));
    }

    @FXML
    protected void onKeyTyped() {
        tryTo();
    }

    @FXML
    protected void onSignSelectButtonClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(DESKTOP));
            File selectedDirectory = fileChooser.showOpenDialog(stage);

            if (selectedDirectory.isFile()) {
                sign.setDocumentPath(selectedDirectory.getAbsolutePath());
                tryTo();
                wrongLabel.setVisible(false);
            }
        } catch (Exception e) {
            wrongLabel.setVisible(true);
        }
    }

    @FXML
    protected void onSignButtonClick() throws Exception {
        signed = true;
        sign.setPin(passwordField.getText());
        sign.init();
    }

    @FXML
    protected void onVerifyDocumentSelectButtonClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(DESKTOP));
            File selectedDirectory = fileChooser.showOpenDialog(stage);

            if (selectedDirectory.isFile()) {
                verify.setDocumentPath(selectedDirectory.getAbsolutePath());
                tryToVerify();
            }
        } catch (Exception e) {
            wrongLabel.setVisible(true);
        }
    }

    @FXML
    protected void onVerifyKeySelectButtonClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(DESKTOP));
            File selectedDirectory = fileChooser.showOpenDialog(stage);

            if (selectedDirectory.isFile()) {
                verify.setKeyPath(selectedDirectory.getAbsolutePath());
                tryToVerify();
            }
        } catch (Exception e) {
            wrongLabel.setVisible(true);
        }
    }

    @FXML
    protected void onVerifyButtonClick() throws Exception {
        verify.init();
    }
}