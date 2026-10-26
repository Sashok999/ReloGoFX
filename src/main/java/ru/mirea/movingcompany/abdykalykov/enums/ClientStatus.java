package ru.mirea.movingcompany.abdykalykov.enums;

public enum ClientStatus {
    NEW("Новый клиент"),
    ACTIVE("Активный клиент"),
    REGULAR("Постоянный клиент"),
    BLOCKED("Заблокирован");

    private String statusName;

    ClientStatus(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }
}
