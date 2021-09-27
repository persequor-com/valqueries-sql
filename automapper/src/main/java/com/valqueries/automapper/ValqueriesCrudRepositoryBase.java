/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 
 */
package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.IStatement;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import com.valqueries.OrmResultSet;
import com.valqueries.UpdateResult;
import io.ran.Clazz;
import io.ran.CompoundKey;
import io.ran.GenericFactory;
import io.ran.Mapping;
import io.ran.MappingHelper;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import io.ran.token.Token;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValqueriesCrudRepositoryBase<T, K> implements ValqueriesBaseCrudRepository<T, K> {
	protected Database database;
	protected GenericFactory genericFactory;
	protected Class<T> modelType;
	protected Class<K> keyType;
	protected TypeDescriber<T> typeDescriber;
	protected MappingHelper mappingHelper;
	private SqlNameFormatter sqlNameFormatter;

	public ValqueriesCrudRepositoryBase(Database database, GenericFactory genericFactory, Class<T> modelType, Class<K> keyType, MappingHelper mappingHelper, SqlNameFormatter sqlNameFormatter) {
		this.database = database;
		this.genericFactory = genericFactory;
		this.modelType = modelType;

		this.keyType = keyType;
		this.typeDescriber = TypeDescriberImpl.getTypeDescriber(modelType);
		this.mappingHelper = mappingHelper;
		this.sqlNameFormatter = sqlNameFormatter;
	}

	private void setKey(IStatement b, K id) {
		if (keyType == CompoundKey.class) {
			CompoundKey key = (CompoundKey) id;

		} else if (keyType == String.class) {
			b.set(keyColumn(), (String) id);
		} else if (keyType == UUID.class) {
			b.set(keyColumn(),(UUID)id);
		} else {
			throw new RuntimeException("So far unhandled key type: "+keyType.getName());
		}
	}

	protected String keyColumn() {
		return "id";
	}

	protected T hydrate(OrmResultSet row) {
		T t = genericFactory.get(modelType);
		((Mapping)t).hydrate(new ValqueriesHydrator(row));
		return t;
	}

	@Override
	public Optional<T> get(K id) {
		return database.obtainInTransaction(tx -> {
			return tx.query("select * from "+getTableName()+" where "+typeDescriber.primaryKeys().get(0).getToken().snake_case()+" = :id", b -> {
				setKey(b, id);
			}, this::hydrate).stream().findFirst();
		});
	}

	@Override
	public Stream<T> getAll() {
		return database.obtainInTransaction(tx -> {
			return tx.query("select * from "+getTableName()+"", this::hydrate).stream();
		});
	}

	@Override
	public CrudUpdateResult deleteById(K id) {
		return getUpdateResult(database.obtainInTransaction(tx -> {
			return tx.update("DELETE from "+getTableName()+" where "+typeDescriber.primaryKeys().get(0).getToken().snake_case()+" = :id", b -> {
				setKey(b, id);
			});
		}));
	}

	@Override
	public CrudUpdateResult save(T t) {
		return database.obtainInTransaction(tx -> {
			return save(tx, t);
		});
	}

	public CrudUpdateResult save(ITransactionContext tx, T t) {
		ValqueriesColumnizer<T> columnizer = new ValqueriesColumnizer<T>(genericFactory, mappingHelper,t, sqlNameFormatter);
		String sql = "INSERT INTO "+getTableName()+" SET "+columnizer.getSql();
		if (!columnizer.getSqlWithoutKey().isEmpty()) {
			sql += " on duplicate key update "+columnizer.getSqlWithoutKey();
		}
		return getUpdateResult(tx.update(sql, columnizer));
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> ts) {
		if (ts.isEmpty()) {
			return () -> 0;
		}
		CompoundColumnizer<T> columnizer = new CompoundColumnizer<T>(genericFactory, mappingHelper,ts, sqlNameFormatter);
		String sql = "INSERT INTO "+getTableName()+" ("+columnizer.getColumns().stream().map(s -> "`"+s+"`").collect(Collectors.joining(", "))+") values "+(columnizer.getValueTokens().stream().map(tokens -> "("+tokens.stream().map(t -> ":"+t).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", ")));

		if (!columnizer.getColumnsWithoutKey().isEmpty()) {
			sql += " on duplicate key update "+columnizer.getColumnsWithoutKey().stream().map(column -> "`"+column+"` = VALUES(`"+column+"`)").collect(Collectors.joining(", "));
		}
		return getUpdateResult(tx.update(sql, columnizer));
	}

	private CrudUpdateResult getUpdateResult(UpdateResult update) {
		return new CrudUpdateResult() {
			@Override
			public int affectedRows() {
				return update.getAffectedRows();
			}
		};
	}

	@Override
	public ValqueriesQueryImpl<T> query() {
		return new ValqueriesQueryImpl<T>(database.getOrm(), modelType, genericFactory, sqlNameFormatter);
	}

	public ValqueriesQueryImpl<T> query(ITransactionContext tx) {
		return new ValqueriesQueryImpl<T>(tx, modelType, genericFactory, sqlNameFormatter);
	}

	@Override
	public <X> X obtainInTransaction(ITransactionWithResult<X> tx) {
		return database.obtainInTransaction(tx);
	}

	protected String getTableName() {
		return getTableName(Clazz.of(modelType));
	}

	static String getTableName(Clazz<?> modeltype) {
		return Token.CamelCase(modeltype.clazz.getSimpleName()).snake_case();
	}


}
