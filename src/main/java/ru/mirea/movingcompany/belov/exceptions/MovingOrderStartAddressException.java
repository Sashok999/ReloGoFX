package ru.mirea.movingcompany.belov.exceptions;

public class MovingOrderStartAddressException extends RuntimeException {
    public MovingOrderStartAddressException(String message) {
        super("Некорректный стартовый адрес заказа: " + message);
    }
}
