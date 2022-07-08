package com.valqueries.automapper;

import io.ran.DbName;
import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

@Mapper(dbType = Valqueries.class)
@DbName("via_this_alternative_name")
public class ViaAltName {
    @PrimaryKey
    private String sourceId;
    @Relation(fields = "sourceId", relationFields = "id")
    private ViaAltNameSource source;
    @PrimaryKey
    private String targetId;
    @Relation(fields = "targetId", relationFields = "id", autoSave = true)
    private ViaAltNameTarget target;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public ViaAltNameSource getSource() {
        return source;
    }

    public void setSource(ViaAltNameSource source) {
        this.source = source;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public ViaAltNameTarget getTarget() {
        return target;
    }

    public void setTarget(ViaAltNameTarget target) {
        this.target = target;
    }
}
