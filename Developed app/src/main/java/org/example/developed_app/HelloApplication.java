package org.example.developed_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Główna klasa aplikacji JavaFX uruchamiająca GUI do podpisywania i weryfikacji dokumentów PDF.
 *
 * <p>Ładuje plik FXML z interfejsem użytkownika, tworzy i wyświetla główne okno aplikacji.</p>
 */
public class HelloApplication extends Application {

    /**
     * Punkt startowy aplikacji.
     *
     * @param args argumenty linii poleceń (nieużywane)
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Metoda startowa aplikacji JavaFX.
     *
     * <p>Ładuje layout z pliku FXML, tworzy scenę i wyświetla okno.</p>
     *
     * @param stage główne okno aplikacji
     * @throws IOException gdy wystąpi problem z załadowaniem pliku FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 320);
        stage.setTitle("Sign/verify PDF");
        stage.setScene(scene);
        stage.show();
    }
}
