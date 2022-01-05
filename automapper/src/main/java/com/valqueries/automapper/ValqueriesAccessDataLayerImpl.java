/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 
 */
package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.IStatement;
import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import com.valqueries.OrmResultSet;
import com.valqueries.UpdateResult;
import io.ran.Clazz;
import io.ran.CompoundKey;
import io.ran.GenericFactory;
import io.ran.MappingHelper;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ValqueriesAccessDataLayerImpl<T, K> implements ValqueriesAccessDataLayer<T, K> {
	protected Database database;
	protected GenericFactory genericFactory;
	protected Class<T> modelType;
	protected Class<K> keyType;
	protected TypeDescriber<T> typeDescriber;
	protected MappingHelper mappingHelper;
	private final SqlNameFormatter sqlNameFormatter;

	public ValqueriesAccessDataLayerImpl(Database database, GenericFactory genericFactory, Class<T> modelType, Class<K> keyType, MappingHelper mappingHelper, SqlNameFormatter sqlNameFormatter) {
		this.database = database;
		this.genericFactory = genericFactory;
		this.modelType = modelType;

		this.keyType = keyType;
		this.typeDescriber = TypeDescriberImpl.getTypeDescriber(modelType);
		this.mappingHelper = mappingHelper;
		this.sqlNameFormatter = sqlNameFormatter;
	}

	private void setKey(IStatement b, K id) {
		setKey(b, id, 0);
	}

	private void setKey(IStatement b, K id, int position) {
		if (keyType == CompoundKey.class) {
			CompoundKey key = (CompoundKey) id;

		} else if (keyType == String.class) {
			b.set(getKeyName(position), (String) id);
		} else if (keyType == UUID.class) {
			b.set(getKeyName(position), (UUID) id);
		} else {
			throw new RuntimeException("So far unhandled key type: "+keyType.getName());
		}
	}

	protected String keyColumn() {
		return "id";
	}

	private String getKeyName(int position) {
		if(position == 0) {
			return keyColumn();
		}
		return keyColumn() + position;
	}

	protected T hydrate(OrmResultSet row) {
		T t = genericFactory.get(modelType);
		mappingHelper.hydrate(t, new ValqueriesHydrator(row, sqlNameFormatter));
		return t;
	}

	@Override
	public Optional<T> get(K id) {
		return database.obtainInTransaction(tx -> {
			return tx.query("select * from "+getTableName()+" where "+typeDescriber.primaryKeys().get(0).getToken().snake_case()+" = :"+ getKeyName(0), b -> {
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
			return tx.update("DELETE from "+getTableName()+" where "+typeDescriber.primaryKeys().get(0).getToken().snake_case()+" = :"+ getKeyName(0), b -> {
				setKey(b, id, 0);
			});
		}));
	}

	@Override
	public CrudUpdateResult deleteByIds(Collection<K> ids) {
		String inIdsSql = IntStream.range(0, ids.size()).mapToObj(this::getKeyName).collect(Collectors.joining(", "));
		return getUpdateResult(database.obtainInTransaction(tx ->
			tx.update("DELETE from " + getTableName() +
					" where " + typeDescriber.primaryKeys().get(0).getToken().snake_case() +
					" IN (" + inIdsSql + ")",
					b -> {
						int counter = 0;
						for (K id : ids) {
							setKey(b, id, counter++);
						}
					})
				));
	}

	@Override
	public CrudUpdateResult save(T t) {
		return database.obtainInTransaction(tx -> {
			return save(tx, t);
		});
	}

	private <O> CrudUpdateResult saveInternal(ITransactionContext tx, O t, Class<O> oClass) {
		ValqueriesColumnizer<O> columnizer = new ValqueriesColumnizer<O>(genericFactory, mappingHelper,t, sqlNameFormatter);
		String sql = "INSERT INTO "+getTableName(Clazz.of(oClass))+" SET "+columnizer.getSql();
		if (!columnizer.getSqlWithoutKey().isEmpty()) {
			sql += " on duplicate key update "+columnizer.getSqlWithoutKey();
		} else {
			sql += " on duplicate key update "+columnizer.getSql();
		}
		return getUpdateResult(tx.update(sql, columnizer));
	}

	private <O> CrudUpdateResult saveInternal(ITransactionContext tx, Collection<O> ts, Class<O> oClass) {
		if (ts.isEmpty()) {
			return () -> 0;
		}
		CompoundColumnizer<O> columnizer = new CompoundColumnizer<O>(genericFactory, mappingHelper,ts, sqlNameFormatter);
		String sql = "INSERT INTO "+getTableName(Clazz.of(oClass))+" ("+columnizer.getColumns().stream().map(s -> "`"+s+"`").collect(Collectors.joining(", "))+") values "+(columnizer.getValueTokens().stream().map(tokens -> "("+tokens.stream().map(t -> ":"+t).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", ")));

		if (!columnizer.getColumnsWithoutKey().isEmpty()) {
			sql += " on duplicate key update "+columnizer.getColumnsWithoutKey().stream().distinct().map(column -> "`"+column+"` = VALUES(`"+column+"`)").collect(Collectors.joining(", "));
		} else {
			sql += " on duplicate key update "+columnizer.getColumns().stream().distinct().map(column -> "`"+column+"` = VALUES(`"+column+"`)").collect(Collectors.joining(", "));
		}
		return getUpdateResult(tx.update(sql, columnizer));
	}

	public CrudUpdateResult save(ITransactionContext tx, T t) {
		return saveInternal(tx, t, modelType);
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> ts) {
		return saveInternal(tx, ts, modelType);
	}

	@Override
	public <O> CrudUpdateResult saveOther(ITransactionContext tx, O entity, Class<O> relationClass) {
		return saveInternal(tx, entity, relationClass);
	}

	@Override
	public <O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> entities, Class<O> relationClass) {
		return saveInternal(tx, entities, relationClass);
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
		return new ValqueriesQueryImpl<T>(database.getOrm(), modelType, genericFactory, sqlNameFormatter, mappingHelper);
	}

	@Override
	public <O> ValqueriesQueryImpl<O> query(Class<O> oClass) {
		return new ValqueriesQueryImpl<O>(database.getOrm(), oClass, genericFactory, sqlNameFormatter, mappingHelper);
	}

	public ValqueriesQueryImpl<T> query(ITransactionContext tx) {
		return new ValqueriesQueryImpl<T>(tx, modelType, genericFactory, sqlNameFormatter, mappingHelper);
	}

	@Override
	public <O> ValqueriesQuery<O> query(ITransactionContext tx, Class<O> oClass) {
		return new ValqueriesQueryImpl<O>(tx, oClass, genericFactory, sqlNameFormatter, mappingHelper);
	}

	@Override
	public <X> X obtainInTransaction(ITransactionWithResult<X> tx) {
		return database.obtainInTransaction(tx);
	}

	@Override
	public void doRetryableInTransaction(ITransaction tx) {
		database.doRetryableInTransaction(tx);
	}

	protected String getTableName() {
		return getTableName(Clazz.of(modelType));
	}

	String getTableName(Clazz<? extends Object> modeltype) {
		return sqlNameFormatter.table(modeltype.clazz);
	}


}