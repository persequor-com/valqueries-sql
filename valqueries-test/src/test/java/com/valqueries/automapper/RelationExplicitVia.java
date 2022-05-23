package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

@Mapper(dbType = Valqueries.class)
public class RelationExplicitVia {
    @PrimaryKey
    private String id;
    private String targetId;
    @Relation(fields = "targetId", relationFields = "id", autoSave = true)
    private RelationWithExplicitVia target;
    private String sourceId;
    @Relation(fields = "sourceId", relationFields = "id")
    private RelationWithExplicitVia source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public RelationWithExplicitVia getSource() {
        return source;
    }

    public void setSource(RelationWithExplicitVia source) {
        this.source = source;
    }

    public RelationWithExplicitVia getTarget() {
        return target;
    }

    public void setTarget(RelationWithExplicitVia target) {
        this.target = target;
    }
}
