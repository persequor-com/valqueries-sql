package com.valqueries.automapper;

import javax.inject.Inject;

public class ViaAltNameSourceRepository extends ValqueriesCrudRepositoryImpl<ViaAltNameSource, String> {
    @Inject
    public ViaAltNameSourceRepository(ValqueriesRepositoryFactory factory) {
        super(factory, ViaAltNameSource.class, String.class);
    }
}




