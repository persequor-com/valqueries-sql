package com.valqueries.automapper;

import io.ran.DbName;
import io.ran.Key;
import io.ran.PrimaryKey;
import org.checkerframework.checker.index.qual.IndexFor;

public class TestTableWithChangedKeys {
    @PrimaryKey
    @DbName("theid")
    private String id;
    @Key(name = "colidx")
    @DbName("idxedCol")
    private String indexedColumn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIndexedColumn() {
        return indexedColumn;
    }

    public void setIndexedColumn(String indexedColumn) {
        this.indexedColumn = indexedColumn;
    }
}
