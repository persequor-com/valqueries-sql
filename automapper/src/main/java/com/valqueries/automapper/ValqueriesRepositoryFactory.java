package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.GenericFactory;
import io.ran.MappingHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ValqueriesRepositoryFactory {
	protected Database database;
	protected GenericFactory genericFactory;
	protected MappingHelper mappingHelper;
	private SqlNameFormatter columnFormatter;

	@Inject
	public ValqueriesRepositoryFactory(Database database, GenericFactory genericFactory, MappingHelper mappingHelper, SqlNameFormatter columnFormatter) {
		this.database = database;
		this.genericFactory = genericFactory;
		this.mappingHelper = mappingHelper;
		this.columnFormatter = columnFormatter;
	}

	public <T, K> ValqueriesBaseCrudRepository<T, K> get(Class<T> modelType, Class<K> keyType) {
		return new ValqueriesCrudRepositoryBase<T, K>(database, genericFactory, modelType, keyType, mappingHelper, columnFormatter);
	}
}
