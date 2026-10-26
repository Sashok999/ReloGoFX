module ru.mirea.movingcompany {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    exports ru.mirea.movingcompany;
    opens ru.mirea.movingcompany.controllers to javafx.fxml;
    opens ru.mirea.movingcompany.gall.controllers to javafx.fxml;
    opens ru.mirea.movingcompany.abdykalykov.controllers to javafx.fxml;
    opens ru.mirea.movingcompany.belov.controllers to javafx.fxml;
}
