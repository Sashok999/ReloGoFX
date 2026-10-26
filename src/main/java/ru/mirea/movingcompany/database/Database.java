package ru.mirea.movingcompany.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    public static Connection getConnection() throws SQLException {
        if (System.getenv("DB_URL") == null || System.getenv("DB_USER") == null
                || System.getenv("DB_PASSWORD") == null) {
            throw new SQLException("Задайте DB_URL, DB_USER и DB_PASSWORD в конфигурации запуска.");
        }
        return DriverManager.getConnection(
                System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD")
        );
    }
}
