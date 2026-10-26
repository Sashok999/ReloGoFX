package ru.mirea.movingcompany.belov.exceptions;

public class MovingOrderEndAddressException extends RuntimeException {
    public MovingOrderEndAddressException(String message) {
        super("Некорректный конечный адрес заказа: " + message);
    }
}
