package com.valqueries.automapper;

import io.ran.DbName;
import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.ArrayList;
import java.util.List;

@Mapper(dbType = Valqueries.class)
@DbName("via_the_alt_name_target")
public class ViaAltNameTarget {
    @PrimaryKey
    private String id;
    @Relation(via = ViaAltName.class, fields = "id", relationFields = "targetId")
    private List<ViaAltNameSource> sources = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ViaAltNameSource> getSources() {
        return sources;
    }

    public void setSources(List<ViaAltNameSource> sources) {
        this.sources = sources;
    }
}
