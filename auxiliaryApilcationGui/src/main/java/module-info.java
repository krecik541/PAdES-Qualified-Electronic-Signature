module org.example.auxiliaryapilcationgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.auxiliaryapilcationgui to javafx.fxml;
    exports org.example.auxiliaryapilcationgui;
}