package com.valqueries.automapper;

import javax.inject.Inject;

public class RelationSourceRepository extends ValqueriesCrudRepositoryImpl<RelationWithExplicitVia, String> {
    @Inject
    public RelationSourceRepository(ValqueriesRepositoryFactory factory) {
        super(factory, RelationWithExplicitVia.class, String.class);
    }
}




