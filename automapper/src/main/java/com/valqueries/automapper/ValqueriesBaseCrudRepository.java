package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CrudRepositoryBaseRepo;

import java.util.Collection;

public interface ValqueriesBaseCrudRepository<T, K> extends CrudRepositoryBaseRepo<T,K, ValqueriesQuery<T>> {
	ValqueriesQuery<T> query(ITransactionContext tx);
	CrudUpdateResult save(ITransactionContext tx, T t);
	CrudUpdateResult save(ITransactionContext tx, Collection<T> t);
	<R> CrudUpdateResult saveRelation(ITransactionContext tx, R t, Class<R> oClass);
	<R> CrudUpdateResult saveRelations(ITransactionContext tx, Collection<R> t, Class<R> oClass);
	<X> X obtainInTransaction(ITransactionWithResult<X> tx);
	void doRetryableInTransaction(ITransaction tx);
}
