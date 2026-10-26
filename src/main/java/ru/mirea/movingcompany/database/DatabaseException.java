package ru.mirea.movingcompany.database;

import java.sql.SQLException;

// Repository interfaces remain independent of JDBC and checked SQL exceptions.
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, SQLException cause) {
        super(message, cause);
    }
}
