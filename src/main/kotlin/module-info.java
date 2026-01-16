module org.orange.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens org.orange.demo to javafx.fxml;
    exports org.orange.demo;
}