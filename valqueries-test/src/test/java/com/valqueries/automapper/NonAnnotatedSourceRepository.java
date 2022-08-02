package com.valqueries.automapper;

import javax.inject.Inject;

public class NonAnnotatedSourceRepository  extends ValqueriesCrudRepositoryImpl<NonAnnotatedSource, String> {
    @Inject
    public NonAnnotatedSourceRepository(ValqueriesRepositoryFactory factory) {
        super(factory, NonAnnotatedSource.class, String.class);
    }
}




