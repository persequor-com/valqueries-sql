package com.valqueries.automapper;

import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.UUID;

public class CarWheel {
    @PrimaryKey
    private UUID id;
    private String brand;
    private UUID carId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }
}
