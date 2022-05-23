package com.valqueries.automapper;

import io.ran.DbName;
import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.ArrayList;
import java.util.List;

@Mapper(dbType = Valqueries.class)
@DbName("via_the_alt_name_source")
public class ViaAltNameSource {
    @PrimaryKey
    private String id;
    @Relation(via = ViaAltName.class, fields = "id", relationFields = "sourceId", autoSave = true)
    private List<ViaAltNameTarget> targets = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ViaAltNameTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<ViaAltNameTarget> targets) {
        this.targets = targets;
    }
}
