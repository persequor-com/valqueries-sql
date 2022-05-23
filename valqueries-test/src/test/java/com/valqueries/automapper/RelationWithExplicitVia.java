package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.List;

@Mapper(dbType = Valqueries.class)
public class RelationWithExplicitVia {
    @PrimaryKey
    private String id;
    @Relation(collectionElementType = RelationExplicitVia.class, fields = "id", relationFields = "targetId", autoSave = true)
    private List<RelationExplicitVia> backwards;
    @Relation(collectionElementType = RelationExplicitVia.class, fields = "id", relationFields = "sourceId", autoSave = true)
    private List<RelationExplicitVia> forwards;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<RelationExplicitVia> getBackwards() {
        return backwards;
    }

    public void setBackwards(List<RelationExplicitVia> backwards) {
        this.backwards = backwards;
    }

    public List<RelationExplicitVia> getForwards() {
        return forwards;
    }

    public void setForwards(List<RelationExplicitVia> forwards) {
        this.forwards = forwards;
    }
}
