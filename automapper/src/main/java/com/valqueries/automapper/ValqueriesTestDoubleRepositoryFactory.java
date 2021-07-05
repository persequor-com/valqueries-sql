package com.valqueries.automapper;

import io.ran.GenericFactory;
import io.ran.MappingHelper;
import io.ran.TestDoubleDb;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ValqueriesTestDoubleRepositoryFactory extends ValqueriesRepositoryFactory {
	private TestDoubleDb store;

	@Inject
	public ValqueriesTestDoubleRepositoryFactory(GenericFactory genericFactory, MappingHelper mappingHelper, TestDoubleDb store) {
		super(null, genericFactory, mappingHelper);
		this.store = store;
	}

	public <T, K> ValqueriesBaseCrudRepository<T, K> get(Class<T> modelType, Class<K> keyType) {
		return new ValqueriesCrudRepositoryTestDoubleBase<T, K>(genericFactory, modelType, keyType, mappingHelper, store);
	}
}
