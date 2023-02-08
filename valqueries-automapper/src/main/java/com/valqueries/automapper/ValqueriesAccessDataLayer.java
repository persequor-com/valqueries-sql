package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CrudRepositoryBaseRepo;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;


public interface ValqueriesAccessDataLayer<T, K> extends CrudRepositoryBaseRepo<T,K, ValqueriesQuery<T>> {
	<O> ValqueriesQuery<O> query(Class<O> oClass);
	ValqueriesQuery<T> query(ITransactionContext tx);
	<O> ValqueriesQuery<O> query(ITransactionContext tx, Class<O> oClass);
	CrudUpdateResult save(ITransactionContext tx, T t);

	/**
	 * Inserts a new entry in the database.
	 * Important to notice : no {@link io.ran.Relation} will be persisted. If the entity to be inserted contains relations,
	 * they will have to be inserted manually.
	 * @param tx the transaction context (will be rollback if an error occurs)
	 * @param t the entity to be inserted
	 * @return a {@link CrudUpdateResult} containing the number of rows affected (e.g. 1).
	 * @throws ValqueriesInsertFailedException if the insert statement fails
	 */
	CrudUpdateResult insert(ITransactionContext tx, T t) throws ValqueriesInsertFailedException;
	CrudUpdateResult save(ITransactionContext tx, Collection<T> t);
	CrudUpdateResult insert(ITransactionContext tx, Collection<T> t) throws ValqueriesInsertFailedException;
	<O> CrudUpdateResult saveOther(ITransactionContext tx, O t, Class<O> oClass);
	<O> CrudUpdateResult insertOther(ITransactionContext tx, O t, Class<O> oClass) throws ValqueriesInsertFailedException;
	<O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> t, Class<O> oClass);
	<O> CrudUpdateResult insertOthers(ITransactionContext tx, Collection<O> t, Class<O> oClass) throws ValqueriesInsertFailedException;
	<X> X obtainInTransaction(ITransactionWithResult<X> tx);
	void doRetryableInTransaction(ITransaction tx);

	Optional<T> get(ITransactionContext tx, K k);

	Stream<T> getAll(ITransactionContext tx);

	CrudUpdateResult deleteById(ITransactionContext tx, K k);

	CrudUpdateResult deleteByIds(ITransactionContext tx, Collection<K> collection);
}
