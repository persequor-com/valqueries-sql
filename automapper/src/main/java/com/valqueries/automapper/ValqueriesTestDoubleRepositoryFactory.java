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
	public ValqueriesTestDoubleRepositoryFactory(GenericFactory genericFactory, MappingHelper mappingHelper, TestDoubleDb store, SqlNameFormatter columnFormatter) {
		super(null, genericFactory, mappingHelper, columnFormatter);
		this.store = store;
	}

	public <T, K> ValqueriesAccessDataLayer<T, K> get(Class<T> modelType, Class<K> keyType) {
		return new ValqueriesAccessDataLayerTestDouble<T, K>(genericFactory, modelType, keyType, mappingHelper, store);
	}
}
