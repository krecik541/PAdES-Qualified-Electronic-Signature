package org.example.developed_app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.developed_app.sign.Sign;
import org.example.developed_app.sign.USBDetector;
import org.example.developed_app.verify.Verify;

import java.io.File;
import java.nio.file.Paths;

/**
 * The `HelloController` class manages the user interface for the PDF signing and verification application.
 * It handles UI events, file selection, button states, and integrates with the `Sign`, `Verify`, and `USBDetector` classes.
 */
public class HelloController {

    /**
     * Default path to the user's desktop directory.
     */
    private static final String DESKTOP = Paths.get(System.getProperty("user.home"), "Desktop").toString();

    /**
     * Flag indicating whether the document has been signed.
     */
    public static boolean signed;

    /**
     * Main stage of the JavaFX application.
     */
    static Stage stage;

    /**
     * Object for signing documents.
     */
    private final Sign sign = new Sign();

    /**
     * Object for verifying document signatures.
     */
    private final Verify verify = new Verify();
    @FXML
    private Text correctLabel;

    @FXML
    private Text changedLabel;

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

    /**
     * Constructor initializes the USB detector in a separate thread to monitor USB connections.
     */
    public HelloController() {
        USBDetector usbDetector = new USBDetector();
        usbDetector.sign = sign;
        usbDetector.controller = this;
        Thread thread = new Thread(usbDetector);
        thread.start();
    }

    /**
     * Updates the state of signing buttons based on input data and USB status.
     */
    public void tryTo() {
        signButton.setDisable(!(!passwordField.getText().isEmpty()
                && sign.isPendriveRecognized()
                && !sign.getDocumentPath().isEmpty()));
        pendriveLabel.setDisable(!sign.isPendriveRecognized());
        pendriveLabel.setText(sign.isPendriveRecognized() ? "Pendrive is recognized" : "Pendrive is not recognized");
    }

    /**
     * Updates the state of the verification button based on selected document and key files.
     */
    public void tryToVerify() {
        verifyButton.setDisable((verify.getDocumentPath().isEmpty() && verify.getKeyPath().isEmpty()));
    }

    /**
     * Handles text input in the PIN field and updates the UI state.
     */
    @FXML
    protected void onKeyTyped() {
        tryTo();
    }

    /**
     * Handles the file selection for signing. Opens a file chooser dialog and sets the document path.
     */
    @FXML
    protected void onSignSelectButtonClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(DESKTOP));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null && selectedFile.isFile()) {
                sign.setDocumentPath(selectedFile.getAbsolutePath());
                tryTo();
                wrongLabel.setVisible(false);
            }
        } catch (Exception e) {
            wrongLabel.setVisible(true);
        }
    }

    /**
     * Handles the signing process. Sets the PIN and initiates the signing operation.
     *
     * @throws Exception If an error occurs during signing.
     */
    @FXML
    protected void onSignButtonClick() throws Exception {
        signed = true;
        sign.setPin(passwordField.getText());
        sign.init();
    }

    /**
     * Handles the file selection for verification. Opens a file chooser dialog and sets the document path.
     */
    @FXML
    protected void onVerifyDocumentSelectButtonClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(DESKTOP));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null && selectedFile.isFile()) {
                verify.setDocumentPath(selectedFile.getAbsolutePath());
                tryToVerify();
            }
        } catch (Exception e) {
            wrongLabel.setVisible(true);
        }
    }

    /**
     * Handles the file selection for the verification key. Opens a file chooser dialog and sets the key path.
     */
    @FXML
    protected void onVerifyKeySelectButtonClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(DESKTOP));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null && selectedFile.isFile()) {
                verify.setKeyPath(selectedFile.getAbsolutePath());
                tryToVerify();
            }
        } catch (Exception e) {
            wrongLabel.setVisible(true);
        }
    }

    /**
     * Handles the verification process. Calls the verification method from the `Verify` class.
     *
     * @throws Exception If an error occurs during verification.
     */
    @FXML
    protected void onVerifyButtonClick() throws Exception {
        verify.init(correctLabel, changedLabel);
    }
}
