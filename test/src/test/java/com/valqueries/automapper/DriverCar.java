package com.valqueries.automapper;

import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.UUID;

public class DriverCar {
    @PrimaryKey
    private String driverId;
    @PrimaryKey
    private UUID carId;

    @Relation(fields = "driverId", relationFields = "id")
    private Driver driver;
    @Relation(fields = "carId", relationFields = "id")
    private Car car;

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
