package com.valqueries.automapper;

import io.ran.Key;
import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

@Mapper(dbType = Valqueries.class)
public class GraphNodeLink {
    @PrimaryKey
    private String fromId;
    @PrimaryKey
    private String toId;
    @Relation(fields = "fromId", relationFields = "id")
    private GraphNode from;
    @Relation(fields = "toId", relationFields = "id")
    private GraphNode to;

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public GraphNode getFrom() {
        return from;
    }

    public void setFrom(GraphNode from) {
        this.from = from;
    }

    public GraphNode getTo() {
        return to;
    }

    public void setTo(GraphNode to) {
        this.to = to;
    }
}
