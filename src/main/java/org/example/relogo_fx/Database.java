package org.example.relogo_fx;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Employee> readEmployee(Connection connection) throws SQLException {
        String sql = "select id, full_name, phone, role, salary from employees order by id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Employee> employees = new ArrayList<>();
            while (result.next()) {
                int id = result.getInt("id");
                String fullName = result.getString("full_name");
                String phone = result.getString("phone");
                String role = result.getString("role");
                int salary = result.getInt("salary");
                employees.add(new Employee(id, fullName, phone, role, salary));
            }
            return employees;
        }
    }

    public static boolean deleteEmployee(Connection connection, int id) throws SQLException {
        String sql = "delete from employees where id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

    public static boolean createEmployee(Connection connection, Employee employee) throws SQLException {
        String sql = "insert into employees (full_name, phone, role, salary) values (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.fullName());
            statement.setString(2, employee.phone());
            statement.setString(3, employee.role());
            statement.setInt(4, employee.salary());
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

    public static boolean updateEmployee(Connection connection, Employee employee) throws SQLException {
        String sql = "update employees set full_name = ?, phone = ?, role = ?, salary = ? where id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.fullName());
            statement.setString(2, employee.phone());
            statement.setString(3, employee.role());
            statement.setInt(4, employee.salary());
            statement.setInt(5, employee.id());
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

    public static List<Client> readClient(Connection connection) throws SQLException {
        String sql = "select id, name, phone, email, status from clients order by id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Client> clients = new ArrayList<>();
            while (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                String phone = result.getString("phone");
                String email = result.getString("email");
                String status = result.getString("status");
                clients.add(new Client(id, name, phone, email, status));
            }
            return clients;
        }
    }

    public static boolean createClient(Connection connection, Client client) throws SQLException {
        String sql = "insert into clients (name, phone, email, status) values (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.name());
            statement.setString(2, client.phone());
            statement.setString(3, client.email());
            statement.setString(4, client.status());
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

    public static boolean updateClient(Connection connection, Client client) throws SQLException {
        String sql = "update clients set name = ?, phone = ?, email = ?, status = ? where id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.name());
            statement.setString(2, client.phone());
            statement.setString(3, client.email());
            statement.setString(4, client.status());
            statement.setInt(5, client.id());
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

    public static boolean deleteClient(Connection connection, int id) throws SQLException {
        String sql = "delete from clients where id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

    public static List<MovingOrder> readMovingOrder(Connection connection) throws SQLException {
        String sql = "select id, id_client, id_foreman, mass, start_address, end_address, status "
                + "from moving_orders order by id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<MovingOrder> orders = new ArrayList<>();
            while (result.next()) {
                int id = result.getInt("id");
                int idClient = result.getInt("id_client");
                int idForeman = result.getInt("id_foreman");
                int mass = result.getInt("mass");
                String startAddress = result.getString("start_address");
                String endAddress = result.getString("end_address");
                String status = result.getString("status");
                orders.add(new MovingOrder(id, idClient, idForeman, mass, startAddress, endAddress, status));
            }
            return orders;
        }
    }

    public static boolean createMovingOrder(Connection connection, MovingOrder order) throws SQLException {
        String sql = "insert into moving_orders "
                + "(id_client, id_foreman, mass, start_address, end_address, status) values (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, order.idClient());
            statement.setInt(2, order.idForeman());
            statement.setInt(3, order.mass());
            statement.setString(4, order.startAddress());
            statement.setString(5, order.endAddress());
            statement.setString(6, order.status());
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

    public static boolean updateMovingOrder(Connection connection, MovingOrder order) throws SQLException {
        String sql = "update moving_orders set id_client = ?, id_foreman = ?, mass = ?, "
                + "start_address = ?, end_address = ?, status = ? where id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, order.idClient());
            statement.setInt(2, order.idForeman());
            statement.setInt(3, order.mass());
            statement.setString(4, order.startAddress());
            statement.setString(5, order.endAddress());
            statement.setString(6, order.status());
            statement.setInt(7, order.id());
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

    public static boolean deleteMovingOrder(Connection connection, int id) throws SQLException {
        String sql = "delete from moving_orders where id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            return rows > 0;
        }
    }

}
