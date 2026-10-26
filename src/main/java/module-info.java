module org.example.relogo_fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens org.example.relogo_fx to javafx.fxml;
    exports org.example.relogo_fx;
}