package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

@Mapper(dbType = Valqueries.class)
public class ChildMarriage {
    @PrimaryKey
    private String personId;
    @PrimaryKey
    private String marriageId;
    @Relation(fields = "personId", relationFields = "id")
    private Person person;
    @Relation(fields = "marriageId", relationFields = "id", autoSave = true)
    private Marriage marriage;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getMarriageId() {
        return marriageId;
    }

    public void setMarriageId(String marriageId) {
        this.marriageId = marriageId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Marriage getMarriage() {
        return marriage;
    }

    public void setMarriage(Marriage marriage) {
        this.marriage = marriage;
    }
}
