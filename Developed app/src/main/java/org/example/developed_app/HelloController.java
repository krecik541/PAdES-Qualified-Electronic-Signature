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
 * Kontroler interfejsu użytkownika aplikacji do podpisywania i weryfikacji dokumentów PDF.
 *
 * <p>Zarządza zdarzeniami UI, obsługuje wybór plików, stan przycisków oraz integrację
 * z klasami {@link Sign}, {@link Verify} oraz {@link USBDetector}.</p>
 */
public class HelloController {

    /**
     * Ścieżka do folderu pulpitu użytkownika.
     */
    private static final String DESKTOP = Paths.get(System.getProperty("user.home"), "Desktop").toString();

    /**
     * Flaga wskazująca, czy dokument został podpisany.
     */
    public static boolean signed;
    /**
     * Główne okno aplikacji JavaFX.
     */
    static Stage stage;
    /**
     * Obiekt klasy {@link Sign} do podpisywania dokumentów.
     */
    private final Sign sign = new Sign();
    /**
     * Obiekt klasy {@link Verify} do weryfikacji podpisów.
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
     * Konstruktor klasy.
     * <p>Uruchamia w osobnym wątku detektor pendrive'ów {@link USBDetector} w celu monitorowania
     * podłączenia urządzenia USB z kluczem prywatnym.</p>
     */
    public HelloController() {
        USBDetector usbDetector = new USBDetector();
        usbDetector.sign = sign;
        usbDetector.controller = this;
        Thread thread = new Thread(usbDetector);
        thread.start();
    }

    /**
     * Aktualizuje stan widoczności i dostępności przycisków podpisywania
     * na podstawie wprowadzonych danych oraz stanu pendrive'a.
     */
    public void tryTo() {
        signButton.setDisable(!(!passwordField.getText().isEmpty()
                && sign.isPendriveRecognized()
                && !sign.getDocumentPath().isEmpty()));
        pendriveLabel.setDisable(!sign.isPendriveRecognized());
        pendriveLabel.setText(sign.isPendriveRecognized() ? "Pendrive is recognized" : "Pendrive is not recognized");
    }

    /**
     * Aktualizuje stan przycisku weryfikacji na podstawie wybranych plików dokumentu i klucza.
     */
    public void tryToVerify() {
        verifyButton.setDisable((verify.getDocumentPath().isEmpty() && verify.getKeyPath().isEmpty()));
    }

    /**
     * Metoda wywoływana podczas wpisywania tekstu w polu PIN.
     * Aktualizuje stan UI wywołując {@link #tryTo()}.
     */
    @FXML
    protected void onKeyTyped() {
        tryTo();
    }

    /**
     * Obsługuje kliknięcie przycisku wyboru pliku do podpisania.
     * Otwiera dialog wyboru pliku i ustawia ścieżkę w obiekcie {@link Sign}.
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
     * Obsługuje kliknięcie przycisku podpisywania dokumentu.
     * Ustawia flagę {@code signed}, pobiera PIN i inicjuje proces podpisu.
     *
     * @throws Exception w przypadku błędów podczas podpisywania dokumentu.
     */
    @FXML
    protected void onSignButtonClick() throws Exception {
        signed = true;
        sign.setPin(passwordField.getText());
        sign.init();
    }

    /**
     * Obsługuje kliknięcie przycisku wyboru pliku PDF do weryfikacji.
     * Ustawia ścieżkę dokumentu w obiekcie {@link Verify}.
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
     * Obsługuje kliknięcie przycisku wyboru pliku z kluczem do weryfikacji.
     * Ustawia ścieżkę klucza w obiekcie {@link Verify}.
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
     * Obsługuje kliknięcie przycisku weryfikacji podpisu.
     * Wywołuje metodę weryfikacji podpisu z klasy {@link Verify}.
     *
     * @throws Exception w przypadku błędów podczas weryfikacji.
     */
    @FXML
    protected void onVerifyButtonClick() throws Exception {
        verify.init(correctLabel, changedLabel);
    }
}
