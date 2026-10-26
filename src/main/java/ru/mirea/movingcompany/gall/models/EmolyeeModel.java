package ru.mirea.movingcompany.gall.models;

import ru.mirea.movingcompany.gall.enums.EmployeeRoles;

public class EmolyeeModel {

    private int id;
    private String fullName;
    private String phone;
    private EmployeeRoles role;
    private int salary;

    public EmolyeeModel(int id, String fullName, String phone, EmployeeRoles role, int salary) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public EmployeeRoles getRole() {
        return role;
    }

    public void setRole(EmployeeRoles role) {
        this.role = role;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }
}
