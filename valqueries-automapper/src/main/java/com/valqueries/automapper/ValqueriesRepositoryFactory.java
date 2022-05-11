package com.valqueries.automapper;

import com.sun.tools.javac.jvm.Gen;
import com.valqueries.Database;
import io.ran.GenericFactory;
import io.ran.MappingHelper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.MissingResourceException;

@Singleton
public class ValqueriesRepositoryFactory {
	protected Database database;
	protected GenericFactory genericFactory;
	protected MappingHelper mappingHelper;
	private SqlNameFormatter columnFormatter;
	private DialectFactory dialectFactory;

	@Inject
	public ValqueriesRepositoryFactory(Database database, GenericFactory genericFactory, MappingHelper mappingHelper, SqlNameFormatter columnFormatter, DialectFactory dialectFactory) {
		this.database = database;
		this.genericFactory = genericFactory;
		this.mappingHelper = mappingHelper;
		this.columnFormatter = columnFormatter;
		this.dialectFactory = dialectFactory;
	}

	public <T, K> ValqueriesAccessDataLayer<T, K> get(Class<T> modelType, Class<K> keyType) {
		return new ValqueriesAccessDataLayerImpl<T, K>(database, genericFactory, modelType, keyType, mappingHelper, columnFormatter, dialectFactory);
	}

	public GenericFactory getGenericFactory() {
		return genericFactory;
	}

    public MappingHelper getMappingHelper() {
		return mappingHelper;
    }
}
