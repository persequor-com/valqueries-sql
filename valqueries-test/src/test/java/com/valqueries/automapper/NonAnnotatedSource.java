package com.valqueries.automapper;

import io.ran.Relation;

public class NonAnnotatedSource {
    private String id;
    @Relation(fields = "id", relationFields = "id")
    private NonAnnotatedTarget target;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NonAnnotatedTarget getTarget() {
        return target;
    }

    public void setTarget(NonAnnotatedTarget target) {
        this.target = target;
    }
}
