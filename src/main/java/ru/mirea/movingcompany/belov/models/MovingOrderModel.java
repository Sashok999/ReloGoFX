package ru.mirea.movingcompany.belov.models;

import ru.mirea.movingcompany.belov.enums.MovingOrderStatus;

public class MovingOrderModel {

    private int id;
    private int idClient;
    private int idForeman;
    private int mass;
    private String startAddress;
    private String endAddress;
    private MovingOrderStatus status;

    public MovingOrderModel(int id, int idClient, int idForeman, int mass, String startAddress, String endAddress, MovingOrderStatus status) {
        this.id = id;
        this.idClient = idClient;
        this.idForeman = idForeman;
        this.mass = mass;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdForeman() {
        return idForeman;
    }

    public void setIdForeman(int idForeman) {
        this.idForeman = idForeman;
    }

    public int getMass() {
        return mass;
    }

    public void setMass(int mass) {
        this.mass = mass;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public MovingOrderStatus getStatus() {
        return status;
    }

    public void setStatus(MovingOrderStatus status) {
        this.status = status;
    }
}
