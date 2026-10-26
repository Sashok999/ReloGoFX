package ru.mirea.movingcompany.abdykalykov.exceptions;

public class ClientTitleException extends RuntimeException {
    public ClientTitleException(String message) {
        super("Некорректные данные клиента: " + message);
    }
}
