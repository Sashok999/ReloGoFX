package ru.mirea.movingcompany.belov.services;

import ru.mirea.movingcompany.belov.enums.MovingOrderStatus;
import ru.mirea.movingcompany.belov.models.MovingOrderModel;
import ru.mirea.movingcompany.belov.repositories.MovingOrderRepository;

import java.util.List;
import java.util.Optional;

public class MovingOrderService {
    private MovingOrderRepository movingOrderRepository;

    public MovingOrderService(MovingOrderRepository movingOrderRepository) {
        this.movingOrderRepository = movingOrderRepository;
    }

    public List<MovingOrderModel> getAllMovingOrder() {
        return movingOrderRepository.getAllMovingOrders();
    }

    public Optional<MovingOrderModel> getMovingOrderById(int id) {
        return movingOrderRepository.getMovingOrderById(id);
    }

    public List<MovingOrderModel> getMovingOrderByStatus(MovingOrderStatus status) {
        return movingOrderRepository.getMovingOrderByStatus(status);
    }

    public List<MovingOrderModel> getMovingOrderByMass(int mass) {
        return movingOrderRepository.getMovingOrderByMass(mass);
    }

    public List<MovingOrderModel> getMovingOrderByIdForeman (int idForeman) {
        return movingOrderRepository.getMovingOrderByIdForeman(idForeman);
    }

    public List<MovingOrderModel> getMovingOrderByIdClient (int idClient) {
        return movingOrderRepository.getMovingOrderByIdClient(idClient);
    }

    public boolean createMovingOrder(MovingOrderModel movingOrder)
    {
        return movingOrderRepository.createMovingOrder(movingOrder);
    }

    public MovingOrderModel updateMovingOrder(MovingOrderModel movingOrder) {
        return movingOrderRepository.updateMovingOrder(movingOrder);
    }

    public  MovingOrderModel deleteMovingOrderById(int id) {
        return movingOrderRepository.deleteMovingOrderById(id);
    }

    public MovingOrderModel updateStatus(int id, MovingOrderStatus status) {
        Optional<MovingOrderModel> movingOrderModelOptional = movingOrderRepository.getMovingOrderById(id);
        if(movingOrderModelOptional.isEmpty()) {
            return null;
        }
        MovingOrderModel movingOrderModel = movingOrderModelOptional.get();
        movingOrderModel.setStatus(status);
        return this.updateMovingOrder(movingOrderModel);
    }
}
