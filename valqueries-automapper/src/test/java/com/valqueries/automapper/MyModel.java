package com.valqueries.automapper;

import io.ran.PrimaryKey;

import java.time.ZonedDateTime;

public class MyModel {
    @PrimaryKey
    private String id;
    private String title;
    private ZonedDateTime updatedAt;

    public MyModel(String id, String title, ZonedDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.updatedAt = updatedAt;
    }

    public MyModel() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
