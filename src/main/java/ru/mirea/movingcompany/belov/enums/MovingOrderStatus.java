package ru.mirea.movingcompany.belov.enums;

public enum MovingOrderStatus {
    IN_PROCESSING("Заказ находится в обработке"),
    IN_PROGRESS("Заказ выполняется"),
    FINISHED("Заказ завершен");


    private String orderStatus;

    MovingOrderStatus(String statusName) {
        this.orderStatus = statusName;
    }

}
