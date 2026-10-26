package ru.mirea.movingcompany.abdykalykov.repositories;

import ru.mirea.movingcompany.abdykalykov.enums.ClientStatus;
import ru.mirea.movingcompany.abdykalykov.models.ClientModel;
import ru.mirea.movingcompany.database.Database;
import ru.mirea.movingcompany.database.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcClientRepository implements ClientRepository {

    private static final String COLUMNS = "id, name, phone, email, status";

    @Override
    public List<ClientModel> getAllClients() {
        return query("select " + COLUMNS + " from clients order by id");
    }

    @Override
    public Optional<ClientModel> getClientById(int id) {
        return query("select " + COLUMNS + " from clients where id = ?", id).stream().findFirst();
    }

    @Override
    public List<ClientModel> getClientsByStatus(ClientStatus status) {
        return query("select " + COLUMNS + " from clients where status is not distinct from ? order by id", status == null ? null : status.name());
    }

    @Override
    public Optional<ClientModel> getClientByPhone(String phone) {
        return query("select " + COLUMNS + " from clients where phone = ? order by id", phone).stream().findFirst();
    }

    @Override
    public List<ClientModel> getClientsByName(String name) {
        return query("select " + COLUMNS + " from clients where lower(name) = lower(?) order by id", name);
    }

    @Override
    public boolean createClient(ClientModel client) {
        String sql = "insert into clients (name, phone, email, status) values (?, ?, ?, ?) returning id";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getName());
            statement.setString(2, client.getPhone());
            statement.setString(3, client.getEmail());
            statement.setString(4, client.getStatus() == null ? null : client.getStatus().name());
            int id;
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return false;
                }
                id = result.getInt("id");
            }
            client.setId(id);
            return true;
        } catch (SQLException exception) {
            throw new DatabaseException("Не удалось создать запись в clients.", exception);
        }
    }

    @Override
    public ClientModel updateClient(ClientModel client) {
        String sql = "update clients set name = ?, phone = ?, email = ?, status = ? where id = ? returning " + COLUMNS;
        return query(sql, client.getName(), client.getPhone(), client.getEmail(), client.getStatus() == null ? null : client.getStatus().name(), client.getId()).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Запись с ID " + client.getId() + " не существует."));
    }

    @Override
    public ClientModel deleteClientById(int id) {
        return query("delete from clients where id = ? returning " + COLUMNS, id).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Запись с ID " + id + " не существует."));
    }

    private List<ClientModel> query(String sql, Object... parameters) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                List<ClientModel> records = new ArrayList<>();
                while (result.next()) {
                    records.add(readModel(result));
                }
                return records;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Не удалось выполнить запрос к clients.", exception);
        }
    }

    private ClientModel readModel(ResultSet result) throws SQLException {
        String enumValue = result.getString("status");
        ClientStatus status = enumValue == null ? null : ClientStatus.valueOf(enumValue);
        return new ClientModel(result.getInt("id"), result.getString("name"), result.getString("phone"), result.getString("email"), status);
    }
}
