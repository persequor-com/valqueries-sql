package com.valqueries.automapper;

import javax.inject.Inject;

public class ObjectWithSerializedFieldRepository extends ValqueriesCrudRepositoryImpl<ObjectWithSerializedField, String> {
	@Inject
	public ObjectWithSerializedFieldRepository(ValqueriesRepositoryFactory factory) {
		super(factory, ObjectWithSerializedField.class, String.class);
	}
}
