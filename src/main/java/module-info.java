module Project.main {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens ru.gosarcho to javafx.fxml;
    exports ru.gosarcho;
}