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
import java.util.Collections;
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
	private final SqlDialect dialect;

	public ValqueriesAccessDataLayerImpl(Database database, GenericFactory genericFactory, Class<T> modelType, Class<K> keyType, MappingHelper mappingHelper, SqlNameFormatter sqlNameFormatter, DialectFactory dialectFactory) {
		this.database = database;
		this.genericFactory = genericFactory;
		this.modelType = modelType;

		this.keyType = keyType;
		this.typeDescriber = TypeDescriberImpl.getTypeDescriber(modelType);
		this.mappingHelper = mappingHelper;
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialectFactory.get(database);
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
		mappingHelper.hydrate(t, new ValqueriesHydrator(row, sqlNameFormatter, dialect, typeDescriber));
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
		String inIdsSql = IntStream.range(0, ids.size()).mapToObj(id -> ":" + getKeyName(id)).collect(Collectors.joining(", "));
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
		return saveInternal(tx, Collections.singletonList(t), oClass);
//		ValqueriesColumnizer<O> columnizer = new ValqueriesColumnizer<O>(genericFactory, mappingHelper,t, sqlNameFormatter);
//
//		/**
//		 *     MERGE dbo.AccountDetails AS myTarget
//		 *     USING (SELECT @Email Email, @Etc etc) AS mySource
//		 *         ON mySource.Email = myTarget.Email
//		 *     WHEN MATCHED THEN UPDATE
//		 *         SET etc = mySource.etc
//		 *     WHEN NOT MATCHED THEN
//		 *         INSERT (Email, Etc)
//		 *         VALUES (@Email, @Etc);
//		 */
//
//		String sql = "MERGE "+getTableName(Clazz.of(oClass))+" as target USING " +
//				"(SELECT "+columnizer.getFields().entrySet().stream().map((e) -> ":"+e.getKey()+" ["+e.getValue()+"]").collect(Collectors.joining(", "))+
//				") as incoming on "+columnizer.getFields().entrySet().stream().map(e -> "target.["+e.getValue()+"] = incoming.["+e.getValue()+"]").collect(Collectors.joining(" AND "))+
//
//				(columnizer.getFieldsWithoutKeys().size() > 0 ? " WHEN MATCHED THEN UPDATE SET "+columnizer.getFieldsWithoutKeys().entrySet().stream().map(e -> "["+e.getValue()+"] = incoming.["+e.getValue()+"]").collect(Collectors.joining(", ")):"")+
//
//				" WHEN NOT MATCHED THEN INSERT ("+columnizer.getFields().entrySet().stream().map(e -> "["+e.getValue()+"]").collect(Collectors.joining(", "))+") " +
//				"VALUES ("+columnizer.getFields().entrySet().stream().map(e -> ":"+e.getKey()+"").collect(Collectors.joining(", "))+");";
////		if (!columnizer.getSqlWithoutKey().isEmpty()) {
////			sql += " on duplicate key update "+columnizer.getSqlWithoutKey();
////		} else {
////			sql += " on duplicate key update "+columnizer.getSql();
////		}
//		return getUpdateResult(tx.update(sql, columnizer));
	}

	private <O> CrudUpdateResult saveInternal(ITransactionContext tx, Collection<O> ts, Class<O> oClass) {
		if (ts.isEmpty()) {
			return () -> 0;
		}
		CompoundColumnizer<O> columnizer = new CompoundColumnizer<O>(genericFactory, mappingHelper,ts, sqlNameFormatter,dialect, TypeDescriberImpl.getTypeDescriber(oClass));


		String sql = dialect.getUpsert(columnizer, oClass);

		return getUpdateResult(tx.update(sql, columnizer));
	}

	private <O> CrudUpdateResult insertInternal(ITransactionContext tx, Collection<O> ts, Class<O> oClass) throws ValqueriesInsertFailedException {
		if (ts.isEmpty()) {
			return () -> 0;
		}
		CompoundColumnizer<O> columnizer = new CompoundColumnizer<O>(genericFactory, mappingHelper, ts, sqlNameFormatter,dialect,TypeDescriberImpl.getTypeDescriber(oClass));
		String sql = dialect.getInsert(columnizer, oClass);
		UpdateResult result;
		try {
			result = tx.update(sql, columnizer); 
		} catch (Exception e){
			throw new ValqueriesInsertFailedException(e);
		}
		
		return getUpdateResult(result);
	}

	public CrudUpdateResult save(ITransactionContext tx, T t) {
		return saveInternal(tx, t, modelType);
	}

	@Override
	public CrudUpdateResult insert(ITransactionContext tx, T t) throws ValqueriesInsertFailedException {
		return insertInternal(tx, Collections.singletonList(t), modelType);
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> ts) {
		return saveInternal(tx, ts, modelType);
	}

	@Override
	public CrudUpdateResult insert(ITransactionContext tx, Collection<T> ts) throws ValqueriesInsertFailedException {
		return insertInternal(tx, ts, modelType);
	}

	@Override
	public <O> CrudUpdateResult saveOther(ITransactionContext tx, O entity, Class<O> relationClass) {
		return saveInternal(tx, entity, relationClass);
	}

	@Override
	public <O> CrudUpdateResult insertOther(ITransactionContext tx, O t, Class<O> oClass) throws ValqueriesInsertFailedException {
		return insertInternal(tx, Collections.singletonList(t), oClass);
	}

	@Override
	public <O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> entities, Class<O> relationClass) {
		return saveInternal(tx, entities, relationClass);
	}

	@Override
	public <O> CrudUpdateResult insertOthers(ITransactionContext tx, Collection<O> ts, Class<O> oClass) throws ValqueriesInsertFailedException {
		return insertInternal(tx, ts, oClass);
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
		return new ValqueriesQueryImpl<T>(database.getOrm(), modelType, genericFactory, sqlNameFormatter, mappingHelper, dialect);
	}

	@Override
	public <O> ValqueriesQueryImpl<O> query(Class<O> oClass) {
		return new ValqueriesQueryImpl<O>(database.getOrm(), oClass, genericFactory, sqlNameFormatter, mappingHelper, dialect);
	}

	public ValqueriesQueryImpl<T> query(ITransactionContext tx) {
		return new ValqueriesQueryImpl<T>(tx, modelType, genericFactory, sqlNameFormatter, mappingHelper, dialect);
	}

	@Override
	public <O> ValqueriesQuery<O> query(ITransactionContext tx, Class<O> oClass) {
		return new ValqueriesQueryImpl<O>(tx, oClass, genericFactory, sqlNameFormatter, mappingHelper, dialect);
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
		return dialect.getTableName(modeltype).toSql();
	}


}