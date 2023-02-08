package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CrudRepository;

import java.util.Collection;

public interface InTransactionValqueriesCrudRepository<T, K> extends CrudRepository<T, K> {
	CrudUpdateResult save(T t);

	/**
	 * Inserts a new entry in the database.
	 * Important to notice : no {@link io.ran.Relation} will be persisted. If the entity to be inserted contains relations,
	 * they will have to be inserted manually.
	 * @param tx the transaction context (will be rollback if an error occurs)
	 * @param t the entity to be inserted
	 * @return a {@link CrudUpdateResult} containing the number of rows affected (e.g. 1).
	 * @throws ValqueriesInsertFailedException if the insert statement fails
	 */
	CrudUpdateResult insert(T t) throws ValqueriesInsertFailedException;
	CrudUpdateResult save(Collection<T> t);
	CrudUpdateResult insert(Collection<T> t) throws ValqueriesInsertFailedException;
	<O> CrudUpdateResult saveOther(O t, Class<O> oClass);
	<O> CrudUpdateResult insertOther(O t, Class<O> oClass) throws ValqueriesInsertFailedException;
	<O> CrudUpdateResult saveOthers(Collection<O> t, Class<O> oClass);
	<O> CrudUpdateResult insertOthers(Collection<O> t, Class<O> oClass) throws ValqueriesInsertFailedException;
}
