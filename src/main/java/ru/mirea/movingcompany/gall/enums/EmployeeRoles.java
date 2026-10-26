package ru.mirea.movingcompany.gall.enums;

public enum EmployeeRoles {
    LOADER("Грузчик"),
    DRIVER("Водитель"),
    FOREMAN("Бригадир"),
    MANAGER("Менеджер");

    private String roleName;

    EmployeeRoles(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}