package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.List;
@Mapper(dbType = Valqueries.class)
public class Person {
    @PrimaryKey
    private String id;
    @Relation(collectionElementType = Person.class, via = PersonMarriage.class, fields = "id", relationFields = "personId", autoSave = true)
    private List<Marriage> marriages;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Marriage> getMarriages() {
        return marriages;
    }

    public void setMarriages(List<Marriage> marriages) {
        this.marriages = marriages;
    }
}
