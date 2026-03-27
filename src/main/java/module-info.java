module com.example.mygame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;


    opens com.example.mygame to javafx.fxml;
    exports com.example.mygame;
}