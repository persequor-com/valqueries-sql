package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CrudRepository;
import io.ran.CrudRepositoryBaseRepo;

import java.util.Collection;

public interface ValqueriesBaseCrudRepository<T, K> extends CrudRepositoryBaseRepo<T,K, ValqueriesQuery<T>> {
	ValqueriesQuery<T> query(ITransactionContext tx);
	CrudUpdateResult save(ITransactionContext tx, T t);
	CrudUpdateResult save(ITransactionContext tx, Collection<T> t);
	<O> CrudUpdateResult saveRelation(ITransactionContext tx, O t, Class<O> oClass);
	<O> CrudUpdateResult saveRelations(ITransactionContext tx, Collection<O> t, Class<O> oClass);
	<X> X obtainInTransaction(ITransactionWithResult<X> tx);
	void doRetryableInTransaction(ITransaction tx);
}
