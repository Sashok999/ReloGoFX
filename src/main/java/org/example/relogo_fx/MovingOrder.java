package org.example.relogo_fx;

public record MovingOrder(
        int id,
        int idClient,
        int idForeman,
        int mass,
        String startAddress,
        String endAddress,
        String status)
{}
