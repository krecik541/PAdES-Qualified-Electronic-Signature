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
 * The `USBDetector` class implements `Runnable` and is responsible for monitoring the file system
 * to detect the connection of a USB drive containing an encrypted private key file (`private.enc`).
 */
public class USBDetector implements Runnable {

    /**
     * Object responsible for signing PDF documents.
     */
    public Sign sign;

    /**
     * GUI controller for the application.
     */
    public HelloController controller;

    /**
     * Main loop for detecting USB devices.
     *
     * <p>The loop runs every second, checking the list of available drives.
     * If it finds a `private.enc` file on a removable drive, it marks the USB drive as recognized
     * and sets the path to the key file.</p>
     *
     * <p>After each iteration, the `tryTo()` method of the controller is called
     * in the JavaFX thread context using {@link Platform#runLater(Runnable)}.</p>
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
