package ru.mirea.movingcompany.belov.repositories;

import ru.mirea.movingcompany.belov.enums.MovingOrderStatus;
import ru.mirea.movingcompany.belov.models.MovingOrderModel;

import java.util.List;
import java.util.Optional;

public interface MovingOrderRepository {
    List<MovingOrderModel> getAllMovingOrders();
    Optional<MovingOrderModel> getMovingOrderById(int id);
    List<MovingOrderModel> getMovingOrderByStatus(MovingOrderStatus status);
    List<MovingOrderModel> getMovingOrderByMass(int mass);
    List<MovingOrderModel> getMovingOrderByIdForeman (int idForeman);
    List<MovingOrderModel> getMovingOrderByIdClient (int idClient);

    boolean createMovingOrder(MovingOrderModel movingOrder);
    MovingOrderModel updateMovingOrder(MovingOrderModel movingOrder);

    MovingOrderModel deleteMovingOrderById(int id);
    
}
