package ru.mirea.movingcompany.belov.repositories;

import ru.mirea.movingcompany.belov.enums.MovingOrderStatus;
import ru.mirea.movingcompany.belov.models.MovingOrderModel;
import ru.mirea.movingcompany.database.Database;
import ru.mirea.movingcompany.database.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMovingOrderRepository implements MovingOrderRepository {

    private static final String COLUMNS = "id, id_client, id_foreman, mass, start_address, end_address, status";

    @Override
    public List<MovingOrderModel> getAllMovingOrders() {
        return query("select " + COLUMNS + " from moving_orders order by id");
    }

    @Override
    public Optional<MovingOrderModel> getMovingOrderById(int id) {
        return query("select " + COLUMNS + " from moving_orders where id = ?", id).stream().findFirst();
    }

    @Override
    public List<MovingOrderModel> getMovingOrderByStatus(MovingOrderStatus status) {
        return query("select " + COLUMNS + " from moving_orders where status is not distinct from ? order by id", status == null ? null : status.name());
    }

    @Override
    public List<MovingOrderModel> getMovingOrderByMass(int mass) {
        return query("select " + COLUMNS + " from moving_orders where mass = ? order by id", mass);
    }

    @Override
    public List<MovingOrderModel> getMovingOrderByIdForeman(int idForeman) {
        return query("select " + COLUMNS + " from moving_orders where id_foreman = ? order by id", idForeman);
    }

    @Override
    public List<MovingOrderModel> getMovingOrderByIdClient(int idClient) {
        return query("select " + COLUMNS + " from moving_orders where id_client = ? order by id", idClient);
    }

    @Override
    public boolean createMovingOrder(MovingOrderModel order) {
        String sql = "insert into moving_orders (id_client, id_foreman, mass, start_address, end_address, status) values (?, ?, ?, ?, ?, ?) returning id";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, order.getIdClient());
            statement.setInt(2, order.getIdForeman());
            statement.setInt(3, order.getMass());
            statement.setString(4, order.getStartAddress());
            statement.setString(5, order.getEndAddress());
            statement.setString(6, order.getStatus() == null ? null : order.getStatus().name());
            int id;
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return false;
                }
                id = result.getInt("id");
            }
            order.setId(id);
            return true;
        } catch (SQLException exception) {
            throw new DatabaseException("Не удалось создать запись в moving_orders.", exception);
        }
    }

    @Override
    public MovingOrderModel updateMovingOrder(MovingOrderModel order) {
        String sql = "update moving_orders set id_client = ?, id_foreman = ?, mass = ?, start_address = ?, end_address = ?, status = ? where id = ? returning " + COLUMNS;
        return query(sql, order.getIdClient(), order.getIdForeman(), order.getMass(), order.getStartAddress(), order.getEndAddress(), order.getStatus() == null ? null : order.getStatus().name(), order.getId()).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Запись с ID " + order.getId() + " не существует."));
    }

    @Override
    public MovingOrderModel deleteMovingOrderById(int id) {
        return query("delete from moving_orders where id = ? returning " + COLUMNS, id).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Запись с ID " + id + " не существует."));
    }

    private List<MovingOrderModel> query(String sql, Object... parameters) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                List<MovingOrderModel> records = new ArrayList<>();
                while (result.next()) {
                    records.add(readModel(result));
                }
                return records;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Не удалось выполнить запрос к moving_orders.", exception);
        }
    }

    private MovingOrderModel readModel(ResultSet result) throws SQLException {
        String enumValue = result.getString("status");
        MovingOrderStatus status = enumValue == null ? null : MovingOrderStatus.valueOf(enumValue);
        return new MovingOrderModel(result.getInt("id"), result.getInt("id_client"), result.getInt("id_foreman"), result.getInt("mass"), result.getString("start_address"), result.getString("end_address"), status);
    }
}
