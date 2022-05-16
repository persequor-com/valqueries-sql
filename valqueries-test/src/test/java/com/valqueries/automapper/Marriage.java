package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.Relation;

import java.util.List;
@Mapper(dbType = Valqueries.class)
public class Marriage {
    private String id;
    @Relation(collectionElementType = Person.class,via = PersonMarriage.class, fields = "id", relationFields = "marriageId")
    private List<Person> persons;
    @Relation(collectionElementType = Person.class,via = ChildMarriage.class, fields = "id", relationFields = "marriageId", autoSave = true)
    private List<Person> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public List<Person> getChildren() {
        return children;
    }

    public void setChildren(List<Person> children) {
        this.children = children;
    }
}
