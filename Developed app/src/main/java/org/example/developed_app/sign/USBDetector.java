package org.example.developed_app.sign;

import javafx.application.Platform;
import org.example.developed_app.HelloController;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Thread.sleep;
import static org.example.developed_app.HelloController.signed;

/**
 * Klasa {@code USBDetector} implementuje {@link Runnable} i odpowiada za monitorowanie systemu plików
 * w celu wykrycia podłączenia pendrive'a zawierającego zaszyfrowany klucz prywatny (plik {@code private.enc}).
 *
 * <p>Po wykryciu nośnika USB z odpowiednim plikiem ustawia flagę w obiekcie {@link Sign}
 * oraz ścieżkę do klucza, a następnie wywołuje metodę {@code tryTo()} w kontrolerze GUI.
 */
public class USBDetector implements Runnable {

    /**
     * Obiekt odpowiedzialny za podpisywanie dokumentów PDF.
     */
    public Sign sign;

    /**
     * Kontroler interfejsu graficznego aplikacji.
     */
    public HelloController controller;

    /**
     * Główna pętla wykrywania urządzenia USB.
     *
     * <p>Pętla uruchamia się co sekundę, sprawdzając listę dostępnych dysków.
     * Jeśli znajdzie plik {@code private.enc} na nośniku typu "removable",
     * oznacza pendrive jako rozpoznany i ustawia ścieżkę do pliku z kluczem.
     *
     * <p>Po każdej iteracji wywoływana jest metoda {@code tryTo()} kontrolera
     * w kontekście wątku JavaFX za pomocą {@link Platform#runLater(Runnable)}.
     */
    @Override
    public void run() {
        while (!signed) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            File[] roots = File.listRoots();
            boolean fl = false;
            String tmpPath = "";

            for (File root : roots) {
                try {
                    Path path = root.toPath();
                    FileStore fileStore = Files.getFileStore(path);
                    boolean isUSB = fileStore.toString().toLowerCase().contains("removable");

                    if (isUSB && root.isDirectory()) {
                        File[] files = root.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile() && file.getName().equals("private.enc")) {
                                    fl = true;
                                    tmpPath = file.getPath();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            sign.setPendriveRecognized(fl);
            sign.setKeyPath(tmpPath);

            Platform.runLater(() -> controller.tryTo());
        }
    }
}
