package org.example.developed_app;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.application.Platform;

import static java.lang.Thread.sleep;
import static org.example.developed_app.HelloController.signed;

public class USBDetector implements Runnable {

    protected Sign sign;
    protected HelloController controller;

    @Override
    public void run() {
        while(!signed) {
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
                    if(isUSB && root.isDirectory()) {
                        File[] files = root.listFiles();

                        assert files != null;
                        for(File file : files)
                            if (file.isFile() && file.getName().equals("private.enc")) {
                                fl = true;
                                tmpPath = file.getPath();
                            }
                    }
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
            sign.setPendriveRecognized(fl);
            sign.setKeyPath(tmpPath);
            Platform.runLater(() -> {
                controller.tryTo();
            });
        }
    }
}
