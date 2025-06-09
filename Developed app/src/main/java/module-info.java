module org.example.developed_app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires kernel;
    requires sign;
    requires org.apache.pdfbox;

    opens org.example.developed_app to javafx.fxml;
    exports org.example.developed_app;
    exports org.example.developed_app.sign;
    opens org.example.developed_app.sign to javafx.fxml;
}