package ru.mirea.movingcompany.gall.repositories;

import ru.mirea.movingcompany.gall.enums.EmployeeRoles;
import ru.mirea.movingcompany.gall.models.EmolyeeModel;
import ru.mirea.movingcompany.database.Database;
import ru.mirea.movingcompany.database.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEmployeeRepository implements EmployeeRepository {

    private static final String COLUMNS = "id, full_name, phone, role, salary";

    @Override
    public List<EmolyeeModel> getAllEmployees() {
        return query("select " + COLUMNS + " from employees order by id");
    }

    @Override
    public Optional<EmolyeeModel> getEmployeeById(int id) {
        return query("select " + COLUMNS + " from employees where id = ?", id).stream().findFirst();
    }

    @Override
    public List<EmolyeeModel> getEmployeesByRole(EmployeeRoles role) {
        return query("select " + COLUMNS + " from employees where role is not distinct from ? order by id", role == null ? null : role.name());
    }

    @Override
    public Optional<EmolyeeModel> getEmployeeByPhone(String phone) {
        return query("select " + COLUMNS + " from employees where phone = ? order by id", phone).stream().findFirst();
    }

    @Override
    public List<EmolyeeModel> getEmployeesByName(String name) {
        return query("select " + COLUMNS + " from employees where lower(full_name) = lower(?) order by id", name);
    }

    @Override
    public boolean createEmployee(EmolyeeModel employee) {
        String sql = "insert into employees (full_name, phone, role, salary) values (?, ?, ?, ?) returning id";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getFullName());
            statement.setString(2, employee.getPhone());
            statement.setString(3, employee.getRole() == null ? null : employee.getRole().name());
            statement.setInt(4, employee.getSalary());
            int id;
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return false;
                }
                id = result.getInt("id");
            }
            employee.setId(id);
            return true;
        } catch (SQLException exception) {
            throw new DatabaseException("Не удалось создать запись в employees.", exception);
        }
    }

    @Override
    public EmolyeeModel updateEmployee(EmolyeeModel employee) {
        String sql = "update employees set full_name = ?, phone = ?, role = ?, salary = ? where id = ? returning " + COLUMNS;
        return query(sql, employee.getFullName(), employee.getPhone(), employee.getRole() == null ? null : employee.getRole().name(), employee.getSalary(), employee.getId()).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Запись с ID " + employee.getId() + " не существует."));
    }

    @Override
    public EmolyeeModel deleteEmployeeById(int id) {
        return query("delete from employees where id = ? returning " + COLUMNS, id).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Запись с ID " + id + " не существует."));
    }

    private List<EmolyeeModel> query(String sql, Object... parameters) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                List<EmolyeeModel> records = new ArrayList<>();
                while (result.next()) {
                    records.add(readModel(result));
                }
                return records;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Не удалось выполнить запрос к employees.", exception);
        }
    }

    private EmolyeeModel readModel(ResultSet result) throws SQLException {
        String enumValue = result.getString("role");
        EmployeeRoles role = enumValue == null ? null : EmployeeRoles.valueOf(enumValue);
        return new EmolyeeModel(result.getInt("id"), result.getString("full_name"), result.getString("phone"), role, result.getInt("salary"));
    }
}
