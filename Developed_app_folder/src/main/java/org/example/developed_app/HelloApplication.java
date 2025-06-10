package org.example.developed_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The `HelloApplication` class serves as the entry point for the JavaFX application.
 * It initializes the GUI and sets up the main stage for the PDF signing and verification tool.
 */
public class HelloApplication extends Application {

    /**
     * The main method launches the JavaFX application.
     *
     * @param args Command-line arguments passed to the application (unused).
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Starts the JavaFX application by setting up the main stage and loading the FXML layout.
     *
     * @param stage The primary stage for the application.
     * @throws IOException If an error occurs while loading the FXML file.
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
