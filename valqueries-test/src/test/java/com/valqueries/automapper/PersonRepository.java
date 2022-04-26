package com.valqueries.automapper;

import javax.inject.Inject;

public class PersonRepository extends ValqueriesCrudRepositoryImpl<Person, String> {
    @Inject
    public PersonRepository(ValqueriesRepositoryFactory factory) {
        super(factory, Person.class, String.class);
    }
}
