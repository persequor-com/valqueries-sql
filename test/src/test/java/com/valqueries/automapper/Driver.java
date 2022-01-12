package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.List;

@Mapper(dbType = Valqueries.class)
public class Driver {
    @PrimaryKey
    private String id;
    private String name;
    @Relation(collectionElementType = Car.class, via = DriverCar.class, autoSave = true)
    private List<Car> cars;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }
}
