package ru.mirea.movingcompany.gall.exceptions;

public class EmplyeeTitleException extends RuntimeException {
    public EmplyeeTitleException(String message) {
        super("Некорректные данные сотрудника: " + message);
    }
}
